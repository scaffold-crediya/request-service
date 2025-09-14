package co.com.jhompo.r2dbc.repositories;


import co.com.jhompo.model.status.Status;
import co.com.jhompo.r2dbc.entity.StatusEntity;
import co.com.jhompo.r2dbc.repositories.status.StatusReactiveRepository;
import co.com.jhompo.r2dbc.repositories.status.StatusRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class StatusRepositoryAdapterTest {

    private StatusReactiveRepository repository;
    private TransactionalOperator transactionalOperator;
    private ObjectMapper mapper;
    private StatusRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(StatusReactiveRepository.class);
        transactionalOperator = Mockito.mock(TransactionalOperator.class);
        mapper = mock(ObjectMapper.class);

        adapter = new StatusRepositoryAdapter(repository, mapper, transactionalOperator);

        // transactionalOperator pasa el publisher sin cambios
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionalOperator.transactional(any(Flux.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // mapear de entidad a dominio
        when(mapper.map(any(), eq(Status.class)))
                .thenAnswer(invocation -> {
                    StatusEntity entity = invocation.getArgument(0);
                    Status status = new Status();
                    status.setId(entity.getId());
                    status.setName(entity.getName());
                    return status;
                });
    }

    @Test
    void saveShouldWork() {
        Status status = new Status();
        status.setId(1);
        status.setName("ACTIVE");

        StatusEntity entity = StatusEntity.builder().id(1).name("ACTIVE").build();

        // mapear dominio → entidad
        when(mapper.map(any(Status.class), eq(StatusEntity.class))).thenReturn(entity);
        // simular repository.save
        when(repository.save(any(StatusEntity.class))).thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.save(status))
                .expectNextMatches(s -> s.getName().equals("ACTIVE"))
                .verifyComplete();

        verify(repository).save(any(StatusEntity.class));
    }


    @Test
    void findByIdShouldReturnStatus() {
        StatusEntity entity = StatusEntity.builder().id(1).name("PENDING").build();
        when(repository.findById(1)).thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.findById(1))
                .expectNextMatches(s -> s.getName().equals("PENDING"))
                .verifyComplete();
    }

    @Test
    void findAllShouldReturnStatuses() {
        StatusEntity e1 = StatusEntity.builder().id(1).name("ACTIVE").build();
        StatusEntity e2 = StatusEntity.builder().id(2).name("INACTIVE").build();

        when(repository.findAll()).thenReturn(Flux.just(e1, e2));

        StepVerifier.create(adapter.findAll())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void deleteByIdShouldWork() {
        when(repository.deleteById(1)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteById(1))
                .verifyComplete();

        verify(repository).deleteById(1);
    }

    @Test
    void findByNameShouldReturnStatus() {
        StatusEntity entity = StatusEntity.builder().id(1).name("ACTIVE").build();
        when(repository.findByName("ACTIVE")).thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.findByName("ACTIVE"))
                .expectNextMatches(s -> s.getName().equals("ACTIVE"))
                .verifyComplete();
    }
}
