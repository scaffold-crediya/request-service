package co.com.jhompo.r2dbc.status;


import co.com.jhompo.r2dbc.entity.StatusEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import reactor.core.publisher.Mono;

public interface StatusReactiveRepository
        extends ReactiveCrudRepository<StatusEntity, Integer>, ReactiveQueryByExampleExecutor<StatusEntity> {

    // Spring Data generará la consulta
    Mono<Boolean> existsByName(String name);
}
