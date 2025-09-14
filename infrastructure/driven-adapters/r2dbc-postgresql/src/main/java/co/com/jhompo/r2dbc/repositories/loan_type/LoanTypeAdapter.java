package co.com.jhompo.r2dbc.repositories.loan_type;

import co.com.jhompo.model.loantype.LoanType;
import co.com.jhompo.util.Messages.*;
import co.com.jhompo.model.loantype.gateways.LoanTypeRepository;
import co.com.jhompo.r2dbc.entity.ApplicationTypeEntity;
import co.com.jhompo.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Repository

public class LoanTypeAdapter
        extends ReactiveAdapterOperations<LoanType, ApplicationTypeEntity, Integer, LoanTypeReactiveRepository>
        implements LoanTypeRepository {

    private static final Logger log = LoggerFactory.getLogger(LoanTypeAdapter.class);
    private final TransactionalOperator transactionalOperator;

    public LoanTypeAdapter(LoanTypeReactiveRepository repository, ObjectMapper mapper, TransactionalOperator transactionalOperator) {
        super(repository, mapper, entity -> mapper.map(entity, LoanType.class));
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Mono<LoanType> save(LoanType loanType) {
        log.info("Iniciando guardado en BD para el tipo de aplicación: {}", loanType.getName());
        return transactionalOperator.transactional(
                super.save(loanType)
                        .doOnSuccess(saved -> log.info(SYSTEM.OPERATION_SUCCESS, saved.getName()))
                        .doOnError(e -> log.error(SYSTEM.OPERATION_ERROR, loanType.getName(), e))
        );
    }

    @Override
    public Mono<LoanType> findById(Integer id) {
        return super.findById(id);
    }

    @Override
    public Flux<LoanType> findAll() {
        return super.findAll();
    }

    @Override
    public Mono<Void> deleteById(Integer id) {
        log.info("Iniciando eliminación en BD para el tipo de aplicación ID: {}", id);
        return transactionalOperator.transactional(
                repository.deleteById(id)
                        .doOnSuccess(v -> log.info(APPLICATION_TYPE.DELETED_SUCCESS, id))
                        .doOnError(e -> log.error(APPLICATION_TYPE.DELETE_FAILED, id, e))
        );
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return repository.existsByName(name);
    }
}
