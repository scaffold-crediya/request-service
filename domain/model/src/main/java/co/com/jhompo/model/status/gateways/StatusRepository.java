package co.com.jhompo.model.status.gateways;

import co.com.jhompo.model.status.Status;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StatusRepository {
    Mono<Status> save(Status status);
    Mono<Status> findById(Integer id);
    Flux<Status> findAll();
    Mono<Void> deleteById(Integer id);
    Mono<Boolean> existsByName(String name);
}
