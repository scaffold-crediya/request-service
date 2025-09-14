package co.com.jhompo.r2dbc.repositories.loan_type;

import co.com.jhompo.r2dbc.entity.ApplicationTypeEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;


public interface LoanTypeReactiveRepository
        extends ReactiveCrudRepository<ApplicationTypeEntity, Integer>, ReactiveQueryByExampleExecutor<ApplicationTypeEntity> {

     Mono<Boolean> existsByName(String name);
}