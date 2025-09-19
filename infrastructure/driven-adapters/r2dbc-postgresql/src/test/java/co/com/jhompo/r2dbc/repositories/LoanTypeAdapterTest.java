package co.com.jhompo.r2dbc.repositories;

import co.com.jhompo.model.loantype.LoanType;
import co.com.jhompo.r2dbc.entity.ApplicationTypeEntity;
import co.com.jhompo.r2dbc.repositories.loan_type.LoanTypeAdapter;
import co.com.jhompo.r2dbc.repositories.loan_type.LoanTypeReactiveRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanTypeAdapterTest {

    @Mock
    private LoanTypeReactiveRepository repository;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private TransactionalOperator transactionalOperator;

    @InjectMocks
    private LoanTypeAdapter adapter;

    private LoanType loanType;
    private ApplicationTypeEntity applicationTypeEntity;

    @BeforeEach
    void setUp() {
        adapter = new LoanTypeAdapter(repository, mapper, transactionalOperator);
        loanType = LoanType.builder().id(1).name("TestLoan").build();
        applicationTypeEntity =  ApplicationTypeEntity.builder().id(1).name("TestLoan").build();

        lenient().when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

    }



    @Test
    void save_should_return_saved_loanType_on_success() {
        // Arrange
        when(mapper.map(any(LoanType.class), any())).thenReturn(applicationTypeEntity);
        when(mapper.map(any(ApplicationTypeEntity.class), Mockito.eq(LoanType.class))).thenReturn(loanType);
        when(repository.save(any(ApplicationTypeEntity.class))).thenReturn(Mono.just(applicationTypeEntity));

        // Act & Assert
        StepVerifier.create(adapter.save(loanType))
                .expectNext(loanType)
                .verifyComplete();

        // Verificamos que se llama al método save en el repositorio
        verify(repository, times(1)).save(any(ApplicationTypeEntity.class));
    }



    @Test
    void save_should_handle_error() {
        // Arrange
        when(mapper.map(any(LoanType.class), any())).thenReturn(applicationTypeEntity);
        when(repository.save(any(ApplicationTypeEntity.class))).thenReturn(Mono.error(new RuntimeException("DB error")));

        // Act & Assert
        StepVerifier.create(adapter.save(loanType))
                .expectErrorMatches(e -> e.getMessage().equals("DB error"))
                .verify();

        // Verificamos que se llama al método save en el repositorio
        verify(repository, times(1)).save(any(ApplicationTypeEntity.class));
    }


    @Test
    void deleteById_should_return_void_on_success() {
        // Arrange
        when(repository.deleteById(anyInt())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(adapter.deleteById(1))
                .verifyComplete();

        // Verificamos que se llama al método deleteById en el repositorio
        verify(repository, times(1)).deleteById(1);
    }



    @Test
    void deleteById_should_handle_error() {
        // Arrange
        when(repository.deleteById(anyInt())).thenReturn(Mono.error(new RuntimeException("DB error")));

        // Act & Assert
        StepVerifier.create(adapter.deleteById(1))
                .expectErrorMatches(e -> e.getMessage().equals("DB error"))
                .verify();

        // Verificamos que se llama al método deleteById en el repositorio
        verify(repository, times(1)).deleteById(1);
    }

    @Test
    void shouldFindById() {
        // Given
        Integer id = 1;

        when(repository.findById(id)).thenReturn(Mono.just(applicationTypeEntity));
        when(mapper.map(eq(applicationTypeEntity), eq(LoanType.class))).thenReturn(loanType);

        // When & Then
        StepVerifier.create(adapter.findById(id))
                .expectNext(loanType)
                .verifyComplete();
    }

    @Test
    void shouldFindAll() {
        // Given
        when(repository.findAll()).thenReturn(Flux.just(applicationTypeEntity));
        when(mapper.map(eq(applicationTypeEntity), eq(LoanType.class))).thenReturn(loanType);

        // When & Then
        StepVerifier.create(adapter.findAll())
                .expectNext(loanType)
                .verifyComplete();
    }

    @Test
    void shouldDeleteById() {
        // Given
        Integer id = 1;

        when(repository.deleteById(id)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(adapter.deleteById(id))
                .verifyComplete();

        verify(repository).deleteById(id);
    }

    @Test
    void shouldCheckExistsByName() {
        // Given
        String name = "Personal";
        when(repository.existsByName(name)).thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(adapter.existsByName(name))
                .expectNext(true)
                .verifyComplete();

        verify(repository).existsByName(name);
    }
}