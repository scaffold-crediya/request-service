package co.com.jhompo.usecase.loanapplication;

import co.com.jhompo.model.applicationtype.gateways.ApplicationTypeRepository;
import co.com.jhompo.model.loanapplication.LoanApplication;
import co.com.jhompo.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.jhompo.model.loanapplication.gateways.UserExistenceGateway;
import co.com.jhompo.model.status.Status;
import co.com.jhompo.model.status.gateways.StatusRepository;
import co.com.jhompo.model.loanapplication.dto.LoanApplicationSummaryDTO;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class LoanApplicationUseCase {


    private final UserExistenceGateway verifyEmailExists;
    private final LoanApplicationRepository loanApplicationRepository;
    private final ApplicationTypeRepository applicationTypeRepository;
    private final StatusRepository statusRepository;

    public Mono<LoanApplication> create(LoanApplication loanApplication) {
        return verifyEmailExists.userExistsByEmail(loanApplication.getEmail())
                .flatMap(userExists -> {
                    if (Boolean.FALSE.equals(userExists)) {
                        return Mono.error(new IllegalArgumentException("El solicitante con email " + loanApplication.getEmail()
                                + " no existe en el sistema de autenticación."));
                    }
                    return applicationTypeRepository.findById(loanApplication.getApplicationTypeId());

                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El Tipo de Solicitud no existe.")))
                .flatMap(applicationType -> {
                    // Lógica para encontrar o crear el estado "PENDIENTE_REVISION"
                    return statusRepository.findByName("PENDIENTE_REVISION")
                            .switchIfEmpty(
                                    Mono.defer(() -> statusRepository.save(
                                            Status.builder().name("PENDIENTE_REVISION").description("Pendiente de revisión por un analista").build()
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
                .switchIfEmpty(Mono.error(new RuntimeException("Solicitud no encontrada")))
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
        return loanApplicationRepository.findSummariesByStatus(statusName.toUpperCase(), page, size);
    }

}