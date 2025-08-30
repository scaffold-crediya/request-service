package co.com.jhompo.r2dbc.application;

import co.com.jhompo.r2dbc.entity.LoanApplicationEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

// This file is just an example, you should delete or modify it
public interface LoanApplicationReactiveRepository
        extends ReactiveCrudRepository<LoanApplicationEntity, UUID>, ReactiveQueryByExampleExecutor<LoanApplicationEntity> {

        Mono<Void> deleteById(UUID id);

}
