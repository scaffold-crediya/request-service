package co.com.jhompo.usecase.applicationtype;

import co.com.jhompo.common.Messages.*;
import co.com.jhompo.model.applicationtype.ApplicationType;
import co.com.jhompo.model.applicationtype.gateways.ApplicationTypeRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ApplicationTypeUseCase {

    private final ApplicationTypeRepository repository;

    public Mono<ApplicationType> create(ApplicationType applicationType) {
        //validaciones de negocio
        return repository.save(applicationType);
    }

    public Mono<ApplicationType> getApplicationTypeById(Integer id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(STATUS.NOT_FOUND)));
    }

    public Flux<ApplicationType> getAllStatuses() {
        return repository.findAll();
    }

    public Mono<ApplicationType> updateStatus(ApplicationType aplicationType) {

        return repository.findById(aplicationType.getId())
                .switchIfEmpty(Mono.error(new RuntimeException(STATUS.NOT_FOUND)))
                .flatMap(existingStatus -> repository.save(aplicationType));
    }

    public Mono<Void> deleteStatus(Integer id) {
        return repository.deleteById(id);
    }
}
