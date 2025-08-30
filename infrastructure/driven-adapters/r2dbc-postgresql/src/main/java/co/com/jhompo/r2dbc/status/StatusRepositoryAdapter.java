package co.com.jhompo.r2dbc.status;

import co.com.jhompo.model.status.Status;
import co.com.jhompo.model.status.gateways.StatusRepository;
import co.com.jhompo.r2dbc.entity.StatusEntity;
import co.com.jhompo.r2dbc.helper.ReactiveAdapterOperations;
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

    public StatusRepositoryAdapter(StatusReactiveRepository repository, ObjectMapper mapper, TransactionalOperator transactionalOperator) {
        // Le decimos al helper cómo mapear de la Entidad de BD (StatusEntity) al Objeto de Dominio (Status)
        super(repository, mapper, entity -> mapper.map(entity, Status.class));
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Mono<Status> save(Status status) {
        log.info("Iniciando guardado en BD para el estado: {}", status.getName());
        return transactionalOperator.transactional(
                super.save(status)
                        .doOnSuccess(s -> log.info("Guardado exitoso en BD: {}", s.getName()))
                        .doOnError(e -> log.error("Error al guardar el estado: {}", status.getName(), e))
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
        log.info("Iniciando eliminación en BD para el estado ID: {}", id);
        return transactionalOperator.transactional(
                repository.deleteById(id)
                        .doOnSuccess(v -> log.info("Eliminación exitosa en BD: {}", id))
                        .doOnError(e -> log.error("Error al eliminar el estado: {}", id, e))
        );
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return repository.existsByName(name);
    }

}
