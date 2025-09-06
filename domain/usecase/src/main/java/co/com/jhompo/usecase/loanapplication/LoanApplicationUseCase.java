package co.com.jhompo.usecase.loanapplication;

import co.com.jhompo.model.applicationtype.ApplicationType;
import co.com.jhompo.model.applicationtype.gateways.ApplicationTypeRepository;
import co.com.jhompo.model.gateways.EmailGateway;
import co.com.jhompo.model.gateways.NotificationGateway;
import co.com.jhompo.model.loanapplication.LoanApplication;
import co.com.jhompo.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.jhompo.model.user.User;
import co.com.jhompo.model.user.gateways.UserExistenceGateway;
import co.com.jhompo.model.status.Status;
import co.com.jhompo.model.status.gateways.StatusRepository;
import co.com.jhompo.model.loanapplication.dto.LoanApplicationSummaryDTO;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

import java.util.List;
import java.util.UUID;

import static co.com.jhompo.common.Messages.*;

@RequiredArgsConstructor
public class LoanApplicationUseCase {



    private final LoanApplicationRepository loanApplicationRepository;
    private final ApplicationTypeRepository applicationTypeRepository;
    private final StatusRepository statusRepository;

    private final UserExistenceGateway verifyEmailExists;
    private final EmailGateway emailGateway;
    private final NotificationGateway notificationGateway;

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
                                            Status.builder().name(STATUS.PENDING_REVIEW).description(STATUS.DESCRIPTION_PENDING).build()
                                    ))
                            )
                            .flatMap(pendingStatus -> {
                                // Aquí se asignan el tipo de aplicación y el estado a la solicitud
                                loanApplication.setStatusId(pendingStatus.getId());
                                loanApplication.setApplicationTypeId(applicationType.getId());
                                return loanApplicationRepository.save(loanApplication);
                            });
                });
    }




    public Mono<LoanApplication> update(LoanApplication loanApplication) {
        return loanApplicationRepository.findById(loanApplication.getId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException(LOAN_APPLICATION.NOT_FOUND)))
                .map(existing -> loanApplication) // aquí ya recibes con id
                .flatMap(loanApplicationRepository::save);
    }

    public Flux<LoanApplication> getAll() {
        return loanApplicationRepository.findAll();
    }

    public Mono<LoanApplication> getById(UUID id) {
        return loanApplicationRepository.findById(id);
    }

    public Mono<Void> delete(UUID id) {
        return loanApplicationRepository.deleteById(id);
    }

    public Flux<LoanApplicationSummaryDTO> findByStatusName(String statusName, int page, int size) {
        return loanApplicationRepository.findSummariesByStatus(statusName.toUpperCase(), page, size)
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

    public Mono<Tuple3<LoanApplication, Status, ApplicationType>> updateStatusAndGetDetails(UUID id, Integer statusId) {

        //Actualizar y guardar el préstamo
        Mono<LoanApplication> updatedLoanMono = loanApplicationRepository.findById(id)
                .flatMap(loan -> {
                    loan.setStatusId(statusId);
                    return loanApplicationRepository.save(loan);
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