package co.com.jhompo.r2dbc.repositories;

import co.com.jhompo.model.loanapplication.LoanApplication;
import co.com.jhompo.r2dbc.entity.LoanApplicationEntity;
import co.com.jhompo.r2dbc.repositories.loan_application.LoanApplicationReactiveRepository;
import co.com.jhompo.r2dbc.repositories.loan_application.LoanApplicationRepositoryAdapter;
import co.com.jhompo.r2dbc.repositories.loan_application.projection.LoanSummaryProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationRepositoryAdapterTest {

    @Mock
    private LoanApplicationReactiveRepository repository;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private TransactionalOperator transactionalOperator;

    private LoanApplicationRepositoryAdapter adapter;

    private LoanApplication testLoanApplication;
    private LoanApplicationEntity testEntity;
    private final UUID testId = UUID.randomUUID();
    private final String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        adapter = new LoanApplicationRepositoryAdapter(repository, mapper, transactionalOperator);

        testLoanApplication = LoanApplication.builder()
                .id(testId)
                .email(testEmail)
                .amount(BigDecimal.valueOf(10000))
                .term(12)
                .statusId(1)
                .applicationTypeId(1)
                .build();

        testEntity = new LoanApplicationEntity();
        testEntity.setId(testId);
        testEntity.setEmail(testEmail);
        testEntity.setAmount(BigDecimal.valueOf(10000));
    }

    @Test
    @DisplayName("Debería guardar préstamo exitosamente con transacción")
    void shouldSaveLoanApplicationSuccessfully() {
        // Given
        when(mapper.map(testLoanApplication, LoanApplicationEntity.class))
                .thenReturn(testEntity);
        when(mapper.map(testEntity, LoanApplication.class))
                .thenReturn(testLoanApplication);
        when(repository.save(testEntity))
                .thenReturn(Mono.just(testEntity));
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When & Then
        StepVerifier.create(adapter.save(testLoanApplication))
                .expectNext(testLoanApplication)
                .verifyComplete();

        verify(repository).save(testEntity);
        verify(transactionalOperator).transactional(any(Mono.class));
    }

    @Test
    @DisplayName("Debería manejar error al guardar préstamo")
    void shouldHandleSaveError() {
        // Given
        when(mapper.map(testLoanApplication, LoanApplicationEntity.class))
                .thenReturn(testEntity);
        when(repository.save(testEntity))
                .thenReturn(Mono.error(new RuntimeException("Database error")));
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When & Then
        StepVerifier.create(adapter.save(testLoanApplication))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Debería eliminar préstamo por ID")
    void shouldDeleteLoanApplicationById() {
        // Given
        when(repository.deleteById(testId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(adapter.deleteById(testId))
                .verifyComplete();

        verify(repository).deleteById(testId);
    }

    @Test
    @DisplayName("Debería encontrar préstamos por email y estado")
    void shouldFindLoansByEmailAndStatus() {
        // Given
        Integer statusId = 1;
        when(repository.findByEmailAndStatusId(testEmail, statusId))
                .thenReturn(Flux.just(testEntity));
        when(mapper.map(testEntity, LoanApplication.class))
                .thenReturn(testLoanApplication);

        // When & Then
        StepVerifier.create(adapter.findByEmailAndStatusId(testEmail, statusId))
                .expectNext(testLoanApplication)
                .verifyComplete();

        verify(repository).findByEmailAndStatusId(testEmail, statusId);
    }

    @Test
    @DisplayName("Debería encontrar resúmenes por estado con paginación")
    void shouldFindSummariesByStatus() {
        // Given
        String statusName = "APPROVED";
        int page = 0;
        int size = 10;
        long offset = 0;

        // Mock del record LoanSummaryProjection
        LoanSummaryProjection projection = mock(LoanSummaryProjection.class);
        when(projection.amount()).thenReturn(BigDecimal.valueOf(10000));
        when(projection.term()).thenReturn(12);
        when(projection.email()).thenReturn(testEmail);
        when(projection.loantypename()).thenReturn("Personal Loan");
        when(projection.interest()).thenReturn(15.5);
        when(projection.statusname()).thenReturn("APPROVED");
        when(projection.totalapproveddebt()).thenReturn(BigDecimal.valueOf(5000));

        when(repository.findSummariesByStatus(statusName.toUpperCase(), size, offset))
                .thenReturn(Flux.just(projection));

        // When & Then
        StepVerifier.create(adapter.findSummariesByStatus(statusName, page, size))
                .expectNextMatches(dto ->
                        dto.getEmail().equals(testEmail) &&
                                dto.getAmount().equals(BigDecimal.valueOf(10000)) &&
                                dto.getStatusName().equals("APPROVED")
                )
                .verifyComplete();

        verify(repository).findSummariesByStatus("APPROVED", size, offset);
    }

    @Test
    @DisplayName("Debería retornar lista vacía cuando no encuentra resúmenes")
    void shouldReturnEmptyWhenNoSummariesFound() {
        // Given
        String statusName = "NONEXISTENT";
        int page = 0;
        int size = 10;
        long offset = 0;

        when(repository.findSummariesByStatus(statusName.toUpperCase(), size, offset))
                .thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(adapter.findSummariesByStatus(statusName, page, size))
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería calcular offset correctamente para paginación")
    void shouldCalculateOffsetCorrectlyForPagination() {
        // Given
        String statusName = "PENDING";
        int page = 2;
        int size = 5;
        long expectedOffset = 10; // page * size = 2 * 5

        when(repository.findSummariesByStatus(statusName.toUpperCase(), size, expectedOffset))
                .thenReturn(Flux.empty());

        // When
        StepVerifier.create(adapter.findSummariesByStatus(statusName, page, size))
                .verifyComplete();

        // Then
        verify(repository).findSummariesByStatus("PENDING", size, expectedOffset);
    }
}
