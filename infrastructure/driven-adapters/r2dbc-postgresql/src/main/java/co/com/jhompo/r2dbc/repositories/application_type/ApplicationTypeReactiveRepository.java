package co.com.jhompo.r2dbc.repositories.application_type;

import co.com.jhompo.r2dbc.entity.ApplicationTypeEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;


public interface ApplicationTypeReactiveRepository
        extends ReactiveCrudRepository<ApplicationTypeEntity, Integer>, ReactiveQueryByExampleExecutor<ApplicationTypeEntity> {

     Mono<Boolean> existsByName(String name);
}