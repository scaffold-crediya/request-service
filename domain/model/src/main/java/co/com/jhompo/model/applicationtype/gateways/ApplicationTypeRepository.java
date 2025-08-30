package co.com.jhompo.model.applicationtype.gateways;

import co.com.jhompo.model.applicationtype.ApplicationType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ApplicationTypeRepository {

    Mono<ApplicationType> save(ApplicationType status);
    Mono<ApplicationType> findById(Integer id);
    Flux<ApplicationType> findAll();
    Mono<Void> deleteById(Integer id);
    Mono<Boolean> existsByName(String name);
}
