package co.com.jhompo.api;

import co.com.jhompo.api.dtos.LoanApplicationDTO;
import co.com.jhompo.api.mapper.LoanApplicationMapper;
import co.com.jhompo.model.loanapplication.LoanApplication;
import co.com.jhompo.model.loanapplication.dto.LoanApplicationSummaryDTO;
import co.com.jhompo.usecase.loanapplication.LoanApplicationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanApplicationControllerTest {

    @Mock
    private LoanApplicationUseCase loanApplicationUseCase;

    @Mock
    private LoanApplicationMapper mapper;

    @InjectMocks
    private LoanApplicationController loanApplicationController;

    private LoanApplicationDTO testLoanApplicationDTO;
    private LoanApplication testLoanApplication;
    private LoanApplication testUpdateLoanApplication;
    private LoanApplicationSummaryDTO testSummaryDTO;
    private final String testEmail = "test@example.com";
    private final UUID testId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        testLoanApplicationDTO = LoanApplicationDTO.builder()
                .email(testEmail)
                .amount(BigDecimal.valueOf(10000))
                .term(12)
                .statusId(1)
                .applicationTypeId(1)
                .build();

        testLoanApplication = LoanApplication.builder()
                .id(testId)
                .email(testEmail)
                .amount(BigDecimal.valueOf(10000))
                .term(12)
                .statusId(1)
                .applicationTypeId(1)
                .build();

        testUpdateLoanApplication = LoanApplication.builder()
                .id(testId)
                .email(testEmail)
                .amount(BigDecimal.valueOf(15000))
                .term(24)
                .statusId(2)
                .applicationTypeId(1)
                .build();

        testSummaryDTO = LoanApplicationSummaryDTO.builder()
                .email(testEmail)
                .amount(BigDecimal.valueOf(10000))
                .statusName("PENDING")
                .build();
    }



    @Test
    @DisplayName("Debería obtener todas las solicitudes exitosamente")
    void shouldGetAllLoanApplicationsSuccessfully() {
        // Given
        LoanApplication secondApplication = LoanApplication.builder()
                .id(UUID.randomUUID())
                .email("another@example.com")
                .amount(BigDecimal.valueOf(15000))
                .term(24)
                .statusId(2)
                .applicationTypeId(2)
                .build();

        when(loanApplicationUseCase.getAll()).thenReturn(Flux.just(testLoanApplication, secondApplication));

        // When & Then
        StepVerifier.create(loanApplicationController.getAll())
                .expectNext(testLoanApplication)
                .expectNext(secondApplication)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería obtener solicitud por ID exitosamente")
    void shouldGetLoanApplicationByIdSuccessfully() {
        // Given
        when(loanApplicationUseCase.getById(testId)).thenReturn(Mono.just(testLoanApplication));

        // When & Then
        StepVerifier.create(loanApplicationController.getById(testId))
                .expectNext(testLoanApplication)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería retornar vacío cuando no encuentra solicitud por ID")
    void shouldReturnEmptyWhenNotFoundById() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(loanApplicationUseCase.getById(nonExistentId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(loanApplicationController.getById(nonExistentId))
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería actualizar solicitud exitosamente")
    void shouldUpdateLoanApplicationSuccessfully() {
        // Given
        when(loanApplicationUseCase.update(any(LoanApplication.class)))
                .thenReturn(Mono.just(testUpdateLoanApplication));

        // When & Then
        StepVerifier.create(loanApplicationController.update(testId, testUpdateLoanApplication))
                .expectNext(testUpdateLoanApplication)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería eliminar solicitud exitosamente")
    void shouldDeleteLoanApplicationSuccessfully() {
        // Given
        when(loanApplicationUseCase.delete(testId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(loanApplicationController.delete(testId))
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería obtener resúmenes de solicitudes por estado exitosamente")
    void shouldGetSummariesByStatusSuccessfully() {
        // Given
        String statusName = "PENDING";
        int page = 0;
        int size = 10;

        LoanApplicationSummaryDTO secondSummary = LoanApplicationSummaryDTO.builder()
                .email("another@example.com")
                .amount(BigDecimal.valueOf(20000))
                .statusName("PENDING")
                .build();

        when(loanApplicationUseCase.findByStatusName(statusName, page, size))
                .thenReturn(Flux.just(testSummaryDTO, secondSummary));

        // When & Then
        StepVerifier.create(loanApplicationController.getSummariesByStatus(statusName, page, size))
                .expectNext(testSummaryDTO)
                .expectNext(secondSummary)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería obtener resúmenes vacíos cuando no hay solicitudes con el estado especificado")
    void shouldReturnEmptyWhenNoApplicationsFoundByStatus() {
        // Given
        String statusName = "NONEXISTENT";
        int page = 0;
        int size = 10;

        when(loanApplicationUseCase.findByStatusName(statusName, page, size))
                .thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(loanApplicationController.getSummariesByStatus(statusName, page, size))
                .verifyComplete();
    }



    @Test
    @DisplayName("Debería manejar error del caso de uso en consulta por ID")
    void shouldHandleUseCaseErrorOnFindById() {
        // Given
        RuntimeException expectedError = new RuntimeException("Database connection failed");
        when(loanApplicationUseCase.getById(testId)).thenReturn(Mono.error(expectedError));

        // When & Then
        StepVerifier.create(loanApplicationController.getById(testId))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Debería manejar error del caso de uso en actualización")
    void shouldHandleUseCaseErrorOnUpdate() {
        // Given
        RuntimeException expectedError = new RuntimeException("Update failed");
        when(loanApplicationUseCase.update(any(LoanApplication.class)))
                .thenReturn(Mono.error(expectedError));

        // When & Then
        StepVerifier.create(loanApplicationController.update(testId, testUpdateLoanApplication))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Debería manejar error del caso de uso en eliminación")
    void shouldHandleUseCaseErrorOnDelete() {
        // Given
        RuntimeException expectedError = new RuntimeException("Delete failed");
        when(loanApplicationUseCase.delete(testId)).thenReturn(Mono.error(expectedError));

        // When & Then
        StepVerifier.create(loanApplicationController.delete(testId))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Debería usar parámetros por defecto en getSummariesByStatus")
    void shouldUseDefaultParametersInGetSummariesByStatus() {
        // Given
        String statusName = "APPROVED";
        // Parámetros por defecto: page=0, size=10

        when(loanApplicationUseCase.findByStatusName(statusName, 0, 10))
                .thenReturn(Flux.just(testSummaryDTO));

        // When & Then
        StepVerifier.create(loanApplicationController.getSummariesByStatus(statusName, 0, 10))
                .expectNext(testSummaryDTO)
                .verifyComplete();
    }
}