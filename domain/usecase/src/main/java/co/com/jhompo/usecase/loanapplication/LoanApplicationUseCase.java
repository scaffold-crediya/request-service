package co.com.jhompo.usecase.loanapplication;

import co.com.jhompo.model.loanapplication.LoanApplication;
import co.com.jhompo.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.jhompo.model.loanapplication.gateways.UserExistenceGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class LoanApplicationUseCase {

    private final LoanApplicationRepository repository;
    private final UserExistenceGateway verifyEmailExists;


    public Mono<LoanApplication> create(LoanApplication loanApplication) {
        return verifyEmailExists.userExistsByEmail(loanApplication.getEmail())
                .flatMap(userExists -> {
                    if (Boolean.FALSE.equals(userExists)) {
                        return Mono.error(new IllegalArgumentException("El solicitante con email " + loanApplication.getEmail() + " no existe en el sistema de autenticación."));
                    }
                    return repository.save(loanApplication);
                });
    }


    public Mono<LoanApplication> update(LoanApplication loanApplication) {
        return repository.findById(loanApplication.getId())
                .switchIfEmpty(Mono.error(new RuntimeException("Solicitud no encontrada")))
                .map(existing -> loanApplication) // aquí ya recibes con id
                .flatMap(repository::save);
    }

    public Flux<LoanApplication> getAll() {
        return repository.findAll();
    }

    public Mono<LoanApplication> getById(UUID id) {
        return repository.findById(id);
    }

    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id);
    }


}