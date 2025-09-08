package co.com.jhompo.usecase.loanapplication;

import co.com.jhompo.model.applicationtype.ApplicationType;
import co.com.jhompo.model.applicationtype.gateways.ApplicationTypeRepository;
import co.com.jhompo.model.gateways.EmailGateway;
import co.com.jhompo.model.gateways.NotificationGateway;
import co.com.jhompo.model.loanapplication.LoanApplication;
import co.com.jhompo.model.loanapplication.dto.LoanValidation;
import co.com.jhompo.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.jhompo.model.user.User;
import co.com.jhompo.model.user.gateways.UserExistenceGateway;
import co.com.jhompo.model.status.Status;
import co.com.jhompo.model.status.gateways.StatusRepository;
import co.com.jhompo.model.loanapplication.dto.LoanApplicationSummaryDTO;
import co.com.jhompo.util.AmortizationUtils;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static co.com.jhompo.util.Messages.*;

@RequiredArgsConstructor
public class LoanApplicationUseCase {



    private final LoanApplicationRepository loanRepository;
    private final ApplicationTypeRepository applicationTypeRepository;
    private final StatusRepository statusRepository;

    private final UserExistenceGateway verifyEmailExists;
    private final EmailGateway emailGateway;
    private final NotificationGateway notificationGateway;
    private final TransactionalOperator txOperator;

    public Mono<LoanApplication> create(LoanApplication loanApplication) {
        return verifyEmailExists.userExistsByEmail(loanApplication.getEmail())
                .flatMap(userExists -> {
                    if (Boolean.FALSE.equals(userExists)) {
                        return Mono.error(new IllegalArgumentException(LOAN_APPLICATION.EMAIL_NOT_FOUND));
                    }
                    return applicationTypeRepository.findById(loanApplication.getApplicationTypeId());

                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException(LOAN_APPLICATION.NOT_FOUND)))
                .flatMap(applicationType -> {
                    // Lógica para encontrar o crear el estado "PENDIENTE_REVISION"
                    return statusRepository.findByName(STATUS.PENDING_REVIEW)
                            .switchIfEmpty(
                                    Mono.defer(() -> statusRepository.save(
                                            Status.builder()
                                                    .name(STATUS.PENDING_REVIEW)
                                                    .description(STATUS.DESCRIPTION_PENDING
                                                    ).build()
                                    ))
                            )
                            .flatMap(pendingStatus -> {
                                // Aquí se asignan el tipo de aplicación y el estado a la solicitud
                                loanApplication.setStatusId(pendingStatus.getId());
                                loanApplication.setApplicationTypeId(applicationType.getId());
                                return loanRepository.save(loanApplication)
                                        .flatMap(savedApp ->
                                                Boolean.TRUE.equals(applicationType.isAutomatic_validation())
                                                        ? handleAutomaticValidation(savedApp, applicationType, 0).thenReturn(savedApp)
                                                        : Mono.just(savedApp)
                                        );

                            });
                });
    }




    public Mono<LoanApplication> update(LoanApplication loanApplication) {
        return loanRepository.findById(loanApplication.getId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException(LOAN_APPLICATION.NOT_FOUND)))
                .map(existing -> loanApplication) // aquí ya recibes con id
                .flatMap(loanRepository::save);
    }

    public Flux<LoanApplication> getAll() {
        return loanRepository.findAll();
    }

    public Mono<LoanApplication> getById(UUID id) {
        return loanRepository.findById(id);
    }

    public Mono<Void> delete(UUID id) {
        return loanRepository.deleteById(id);
    }

    public Flux<LoanApplicationSummaryDTO> findByStatusName(String statusName, int page, int size) {
        return loanRepository.findSummariesByStatus(statusName.toUpperCase(), page, size)
                .collectList()
                .flatMapMany(summaries -> {
                    List<String> emails = summaries.stream()
                            .map(LoanApplicationSummaryDTO::getEmail)
                            .distinct()
                            .toList();

                    return verifyEmailExists.findUserDetailsByEmails(emails)
                            .collectMap(User::getEmail, user -> user)
                            .flatMapMany(userMap -> Flux.fromIterable(summaries)
                                    .map(summary -> {
                                        User user = userMap.get(summary.getEmail());
                                        if (user != null) {
                                            summary.setName(user.getFirstName()); // o getName() según tu clase User
                                            summary.setBaseSalary(user.getBaseSalary()); // o el campo correcto
                                        }
                                        return summary;
                                    })
                            );
                });
    }



    public Mono<Tuple3<LoanApplication, Status, ApplicationType>> updateStatusAndGetDetails(UUID applicationId, Integer statusId) {
        return loanRepository.findById(applicationId)
                .switchIfEmpty(Mono.error(new RuntimeException("Loan application not found: " + applicationId)))
                .flatMap(app -> applicationTypeRepository.findById(app.getApplicationTypeId())
                        .flatMap(appType -> {
                            boolean automaticValidation = Boolean.TRUE.equals(appType.isAutomatic_validation());
                            if (automaticValidation) {
                                return handleAutomaticValidation(app, appType, statusId);
                            } else {
                                return handleManualValidation(app, appType, statusId);
                            }
                        })
                );
    }

    private Mono<Tuple3<LoanApplication, Status, ApplicationType>> handleAutomaticValidation(
            LoanApplication app,
            ApplicationType appType,
            Integer requestedStatusId) {

        return verifyEmailExists.findUserDetailsByEmails(List.of(app.getEmail()))
                .next()
                .switchIfEmpty(Mono.error(new RuntimeException("User not found for email: " + app.getEmail())))
                .flatMap(user -> getActiveDebts(user)
                        .flatMap(activeDebts -> {
                            LoanValidation message = buildLoanValidationMessage(app, appType, user, requestedStatusId, activeDebts);
                            return notificationGateway.sendForValidation(message)
                                    .then(Mono.zip(
                                            Mono.just(app),
                                            statusRepository.findById(app.getStatusId()),
                                            Mono.just(appType)
                                    ));
                        })
                );
    }

    private Mono<Tuple3<LoanApplication, Status, ApplicationType>> handleManualValidation(
            LoanApplication app,
            ApplicationType appType,
            Integer statusId) {

        app.setStatusId(statusId);
        return loanRepository.save(app)
                .flatMap(saved -> statusRepository.findById(saved.getStatusId())
                        .flatMap(status ->
                                notificationGateway.sendNotification(
                                        saved.getId().toString(),
                                        status.getName(),
                                        saved.getEmail()
                                ).thenReturn(Tuples.of(saved, status, appType))
                        )
                );
    }

    private Mono<List<Double>> getActiveDebts(User user) {
        return statusRepository.findByName("APROBADO")
                .flatMapMany(approvedStatus -> loanRepository.findByEmailAndStatusId(user.getEmail(), approvedStatus.getId()))
                .flatMap(activeLoan -> {
                    BigDecimal cuota = AmortizationUtils.calculateMonthlyPayment(
                            activeLoan.getAmount(),
                            BigDecimal.valueOf(activeLoan.getTerm()),
                            activeLoan.getTerm()
                    );
                    return Mono.just(cuota);
                })
                .collectList()
                .map(list -> list.stream()
                        .map(BigDecimal::doubleValue)
                        .collect(Collectors.toList())
                );
    }

    private LoanValidation buildLoanValidationMessage(
            LoanApplication app,
            ApplicationType appType,
            User user,
            Integer requestedStatusId,
            List<Double> activeDebts) {

        LoanValidation message = new LoanValidation();
        message.setLoanId(app.getId().toString());
        message.setUserName(user.getFirstName());
        message.setEmail(app.getEmail());
        message.setAmount(app.getAmount().doubleValue());
        message.setTerm(app.getTerm());
        message.setInterestRate(appType.getInterest_rate());
        message.setBaseSalary(user.getBaseSalary().doubleValue());
        message.setActiveDebts(activeDebts);
        message.setRequestedStatusId(requestedStatusId);
        message.setLoanTypeId(appType.getId());
        message.setAutomaticValidation(Boolean.TRUE.equals(appType.isAutomatic_validation()));
        return message;
    }


    public Mono<Tuple3<LoanApplication, Status, ApplicationType>> updateStatusAndGetDetailsOld(UUID id, Integer statusId) {

        //Actualizar y guardar el préstamo
        Mono<LoanApplication> updatedLoanMono = loanRepository.findById(id)
                .flatMap(loan -> {
                    loan.setStatusId(statusId);
                    return loanRepository.save(loan);
                });

        //Obtener los detalles de forma paralela
        Mono<Status> statusMono = updatedLoanMono.flatMap(loan -> statusRepository.findById(loan.getStatusId()));
        Mono<ApplicationType> applicationTypeMono = updatedLoanMono.flatMap(loan -> applicationTypeRepository.findById(loan.getApplicationTypeId()));

        //Combinar todos los Monos en una sola tupla
        return Mono.zip(updatedLoanMono, statusMono, applicationTypeMono)
                .doOnNext(tuple -> {
                    LoanApplication updatedLoan = tuple.getT1();
                    Status newStatus = tuple.getT2();

                    // Llama al metodo de la interfaz. No sabe que por debajo se usa SQS.
                    notificationGateway.sendNotification(
                            updatedLoan.getId().toString(),
                            newStatus.getName(),
                            updatedLoan.getEmail()
                    ).subscribe();
                });
    }

}