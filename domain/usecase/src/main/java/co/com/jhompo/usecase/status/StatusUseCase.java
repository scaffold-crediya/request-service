package co.com.jhompo.usecase.status;

import co.com.jhompo.model.status.Status;
import co.com.jhompo.model.status.gateways.StatusRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class StatusUseCase {

    private final StatusRepository statusRepository;

    public Mono<Status> createStatus(Status status) {
        // Regla de negocio: No permitir la creación de un estado con un nombre duplicado.
        return statusRepository.findByName(status.getName())
                .flatMap(foundStatus ->
                    Mono.<Status>error(new IllegalArgumentException("Status with name '" + status.getName() + "' already exists."))
                )
                .switchIfEmpty(
                    Mono.defer(() -> statusRepository.save(status))
                );
    }

    public Mono<Status> getStatusById(Integer id) {
        return statusRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Status with id " + id + " not found.")));
    }

    public Flux<Status> getAllStatuses() {
        return statusRepository.findAll();
    }

    public Mono<Status> updateStatus(Status status) {
        // Asegura que el estado exista antes de intentar actualizarlo
        return statusRepository.findById(status.getId())
                .switchIfEmpty(Mono.error(new RuntimeException("Status with id " + status.getId() + " not found.")))
                .flatMap(existingStatus -> statusRepository.save(status));
    }

    public Mono<Void> deleteStatus(Integer id) {
        return statusRepository.deleteById(id);
    }

}
