package co.com.jhompo.r2dbc.status;

import co.com.jhompo.common.Messages.*;
import co.com.jhompo.model.status.Status;
import co.com.jhompo.model.status.gateways.StatusRepository;
import co.com.jhompo.r2dbc.entity.StatusEntity;
import co.com.jhompo.r2dbc.helper.ReactiveAdapterOperations;
import co.com.jhompo.r2dbc.mappers.StatusMapper;
import org.reactivecommons.utils.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class StatusRepositoryAdapter
        extends ReactiveAdapterOperations<Status, StatusEntity, Integer, StatusReactiveRepository>
        implements StatusRepository {

    private static final Logger log = LoggerFactory.getLogger(StatusRepositoryAdapter.class);
    private final TransactionalOperator transactionalOperator;
    private final StatusMapper statusMapper;

    public StatusRepositoryAdapter(StatusReactiveRepository repository, ObjectMapper mapper, TransactionalOperator transactionalOperator, StatusMapper statusMapper) {
        // Le decimos al helper cómo mapear de la Entidad de BD (StatusEntity) al Objeto de Dominio (Status)
        super(repository, mapper, entity -> mapper.map(entity, Status.class));
        this.transactionalOperator = transactionalOperator;
        this.statusMapper = statusMapper;
    }

    @Override
    public Mono<Status> save(Status status) {
        log.info(STATUS.DESCRIPTION_CREATE, status.getName());
        return transactionalOperator.transactional(
                super.save(status)
                        .doOnSuccess(s -> log.info(SYSTEM.OPERATION_SUCCESS, s.getName()))
                        .doOnError(e -> log.error(SYSTEM.OPERATION_ERROR, status.getName(), e))
        );
    }

    @Override
    public Mono<Status> findById(Integer id) {
        return super.findById(id);
    }

    @Override
    public Flux<Status> findAll() {
        return super.findAll();
    }

    @Override
    public Mono<Void> deleteById(Integer id) {
        log.info(STATUS.DESCRIPTION_DELETE, id);
        return transactionalOperator.transactional(
                repository.deleteById(id)
                        .doOnSuccess(v -> log.info(STATUS.DELETED_SUCCESS, id))
                        .doOnError(e -> log.error(STATUS.DELETE_FAILED, id, e))
        );
    }

    @Override
    public Mono<Status> findByName(String name) {
        return repository.findByName(name).map(statusMapper::toDomain);
    }



}
