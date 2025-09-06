package co.com.jhompo.r2dbc.repositories.application_type;

import co.com.jhompo.common.Messages.*;
import co.com.jhompo.model.applicationtype.ApplicationType;
import co.com.jhompo.model.applicationtype.gateways.ApplicationTypeRepository;
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

public class ApplicationTypeAdapter
        extends ReactiveAdapterOperations<ApplicationType, ApplicationTypeEntity, Integer, ApplicationTypeReactiveRepository>
        implements ApplicationTypeRepository {

    private static final Logger log = LoggerFactory.getLogger(ApplicationTypeAdapter.class);
    private final TransactionalOperator transactionalOperator;

    public ApplicationTypeAdapter(ApplicationTypeReactiveRepository repository, ObjectMapper mapper, TransactionalOperator transactionalOperator) {
        super(repository, mapper, entity -> mapper.map(entity, ApplicationType.class));
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Mono<ApplicationType> save(ApplicationType applicationType) {
        log.info("Iniciando guardado en BD para el tipo de aplicación: {}", applicationType.getName());
        return transactionalOperator.transactional(
                super.save(applicationType)
                        .doOnSuccess(saved -> log.info(SYSTEM.OPERATION_SUCCESS, saved.getName()))
                        .doOnError(e -> log.error(SYSTEM.OPERATION_ERROR, applicationType.getName(), e))
        );
    }

    @Override
    public Mono<ApplicationType> findById(Integer id) {
        return super.findById(id);
    }

    @Override
    public Flux<ApplicationType> findAll() {
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
