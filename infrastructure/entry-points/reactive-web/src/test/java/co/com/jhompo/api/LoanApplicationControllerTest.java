package co.com.jhompo.api;

import co.com.jhompo.api.dtos.LoanApplicationDTO;
import co.com.jhompo.api.mapper.LoanApplicationMapper;
import co.com.jhompo.model.loanapplication.LoanApplication;
import co.com.jhompo.model.loanapplication.dto.LoanApplicationSummaryDTO;
import co.com.jhompo.model.loantype.LoanType;
import co.com.jhompo.model.status.Status;
import co.com.jhompo.usecase.loanapplication.LoanApplicationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
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

        testSummaryDTO = LoanApplicationSummaryDTO.builder()
                .email(testEmail)
                .amount(BigDecimal.valueOf(10000))
                .statusName("PENDING")
                .build();
    }

    @Test
    @DisplayName("Deberia obtener todas las solicitudes exitosamente")
    void shouldGetAllApplications() {
        when(loanApplicationUseCase.getAll()).thenReturn(Flux.just(testLoanApplication));
        when(mapper.toDto(any(LoanApplication.class))).thenReturn(testLoanApplicationDTO);

        StepVerifier.create(loanApplicationController.getAll())
                .expectNext(testLoanApplicationDTO)
                .verifyComplete();
    }

    @Test
    @DisplayName("Deberia crear solicitud exitosamente cuando email coincide con usuario autenticado")
    void shouldCreateApplicationSuccessfully() {
        when(mapper.toEntityForCreate(testLoanApplicationDTO)).thenReturn(testLoanApplication);
        when(loanApplicationUseCase.create(testLoanApplication)).thenReturn(Mono.just(testLoanApplication));
        when(mapper.toDto(testLoanApplication)).thenReturn(testLoanApplicationDTO);

        Mono<LoanApplicationDTO> result = loanApplicationController.create(testLoanApplicationDTO)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                        new TestingAuthenticationToken(testEmail, null)
                ));

        StepVerifier.create(result)
                .expectNext(testLoanApplicationDTO)
                .verifyComplete();
    }

    @Test
    @DisplayName("Deberia fallar al crear solicitud cuando email no coincide con usuario autenticado")
    void shouldFailCreateWhenEmailDoesNotMatchAuthenticatedUser() {
        LoanApplicationDTO dtoWithDifferentEmail = LoanApplicationDTO.builder()
                .email("other@example.com")
                .amount(BigDecimal.valueOf(10000))
                .term(12)
                .statusId(1)
                .applicationTypeId(1)
                .build();

        Mono<LoanApplicationDTO> result = loanApplicationController.create(dtoWithDifferentEmail)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                        new TestingAuthenticationToken(testEmail, null)
                ));

        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    @DisplayName("Deberia actualizar solicitud exitosamente")
    void shouldUpdateApplicationSuccessfully() {
        when(loanApplicationUseCase.update(any(LoanApplication.class))).thenReturn(Mono.just(testLoanApplication));
        when(mapper.toDto(testLoanApplication)).thenReturn(testLoanApplicationDTO);

        StepVerifier.create(loanApplicationController.update(testId, testLoanApplicationDTO))
                .expectNext(testLoanApplicationDTO)
                .verifyComplete();
    }

    @Test
    @DisplayName("Deberia actualizar estado de solicitud exitosamente")
    void shouldUpdateStatusSuccessfully() {
        Tuple3<LoanApplication, Status, LoanType> tuple =
                Tuples.of(testLoanApplication,
                        Status.builder().id(1).name("APPROVED").build(),
                        LoanType.builder().id(1).name("Personal Loan").build());

        when(loanApplicationUseCase.updateStatusAndGetDetails(testId, 1)).thenReturn(Mono.just(tuple));

        StepVerifier.create(loanApplicationController.updateStatus(testId, 1))
                .expectNextMatches(response -> response.getStatusName().equals("APPROVED"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Deberia obtener solicitud por ID exitosamente")
    void shouldGetByIdSuccessfully() {
        when(loanApplicationUseCase.getById(testId)).thenReturn(Mono.just(testLoanApplication));
        when(mapper.toDto(testLoanApplication)).thenReturn(testLoanApplicationDTO);

        StepVerifier.create(loanApplicationController.getById(testId))
                .expectNext(testLoanApplicationDTO)
                .verifyComplete();
    }

    @Test
    @DisplayName("Deberia manejar error cuando no se encuentra solicitud por ID")
    void shouldHandleErrorOnGetById() {
        when(loanApplicationUseCase.getById(testId)).thenReturn(Mono.error(new RuntimeException("Not found")));

        StepVerifier.create(loanApplicationController.getById(testId))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Deberia eliminar solicitud exitosamente")
    void shouldDeleteSuccessfully() {
        when(loanApplicationUseCase.delete(testId)).thenReturn(Mono.empty());

        StepVerifier.create(loanApplicationController.delete(testId))
                .verifyComplete();
    }

    @Test
    @DisplayName("Deberia manejar error al eliminar solicitud")
    void shouldHandleErrorOnDelete() {
        when(loanApplicationUseCase.delete(testId)).thenReturn(Mono.error(new RuntimeException("Delete failed")));

        StepVerifier.create(loanApplicationController.delete(testId))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Deberia obtener resumenes por estado exitosamente")
    void shouldGetSummariesByStatusSuccessfully() {
        when(loanApplicationUseCase.findByStatusName("PENDING", 0, 10))
                .thenReturn(Flux.just(testSummaryDTO));

        StepVerifier.create(loanApplicationController.getSummariesByStatus("PENDING", 0, 10))
                .expectNext(testSummaryDTO)
                .verifyComplete();
    }

    @Test
    @DisplayName("Deberia manejar error al obtener resumenes por estado")
    void shouldHandleErrorOnGetSummariesByStatus() {
        when(loanApplicationUseCase.findByStatusName("PENDING", 0, 10))
                .thenReturn(Flux.error(new RuntimeException("Error retrieving summaries")));

        StepVerifier.create(loanApplicationController.getSummariesByStatus("PENDING", 0, 10))
                .expectError(RuntimeException.class)
                .verify();
    }

}
