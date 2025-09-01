package co.com.jhompo.usecase;

import co.com.jhompo.model.applicationtype.ApplicationType;
import co.com.jhompo.model.applicationtype.gateways.ApplicationTypeRepository;
import co.com.jhompo.model.loanapplication.LoanApplication;
import co.com.jhompo.model.loanapplication.dto.LoanApplicationSummaryDTO;
import co.com.jhompo.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.jhompo.model.loanapplication.gateways.UserExistenceGateway;
import co.com.jhompo.model.status.Status;
import co.com.jhompo.model.status.gateways.StatusRepository;
import co.com.jhompo.usecase.loanapplication.LoanApplicationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanApplicationUseCaseTest {

    @Mock
    private UserExistenceGateway verifyEmailExists;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private ApplicationTypeRepository applicationTypeRepository;

    @Mock
    private StatusRepository statusRepository;

    @InjectMocks
    private LoanApplicationUseCase loanApplicationUseCase;

    private LoanApplication testLoanApplication;
    private ApplicationType testApplicationType;
    private Status testStatus;
    private LoanApplicationSummaryDTO testSummaryDTO;
    private final UUID testId = UUID.randomUUID();
    private final String testEmail = "test@example.com";
    private final String statusName = "PENDIENTE_REVISION";

    @BeforeEach
    void setUp() {
        testLoanApplication = LoanApplication.builder()
                .id(testId)
                .email(testEmail)
                .amount(BigDecimal.valueOf(10000))
                .term(12)
                .statusId(1)
                .applicationTypeId(1)
                .build();

        testApplicationType = ApplicationType.builder()
                .id(1)
                .name("Personal Loan")
                .minimum_amount(1000.0)
                .maximum_amount(50000.0)
                .interest_rate(15.5)
                .automatic_validation(true)
                .build();

        testStatus = Status.builder()
                .id(1)
                .name(statusName)
                .description("Pendiente de revisión por un analista")
                .build();

        testSummaryDTO = LoanApplicationSummaryDTO.builder()
                .email(testEmail)
                .amount(BigDecimal.valueOf(10000))
                .statusName("PENDING")
                .build();
    }

    @Test
    @DisplayName("Debería crear solicitud exitosamente cuando el usuario existe")
    void shouldCreateLoanApplicationSuccessfully() {
        // Given
        when(verifyEmailExists.userExistsByEmail(testEmail)).thenReturn(Mono.just(true));
        when(applicationTypeRepository.findById(1)).thenReturn(Mono.just(testApplicationType));
        when(statusRepository.findByName(statusName)).thenReturn(Mono.just(testStatus));
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(Mono.just(testLoanApplication));

        // When & Then
        StepVerifier.create(loanApplicationUseCase.create(testLoanApplication))
                .expectNext(testLoanApplication)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería fallar cuando el usuario no existe")
    void shouldFailWhenUserDoesNotExist() {
        // Given
        when(verifyEmailExists.userExistsByEmail(testEmail)).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(loanApplicationUseCase.create(testLoanApplication))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("no existe en el sistema de autenticación")
                )
                .verify();
    }

    @Test
    @DisplayName("Debería fallar cuando el tipo de aplicación no existe")
    void shouldFailWhenApplicationTypeDoesNotExist() {
        // Given
        when(verifyEmailExists.userExistsByEmail(testEmail)).thenReturn(Mono.just(true));
        when(applicationTypeRepository.findById(1)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(loanApplicationUseCase.create(testLoanApplication))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().equals("El Tipo de Solicitud no existe.")
                )
                .verify();
    }

    @Test
    @DisplayName("Debería crear estado PENDIENTE_REVISION si no existe")
    void shouldCreatePendingStatusIfNotExists() {
        // Given
        when(verifyEmailExists.userExistsByEmail(testEmail)).thenReturn(Mono.just(true));
        when(applicationTypeRepository.findById(1)).thenReturn(Mono.just(testApplicationType));
        when(statusRepository.findByName(statusName)).thenReturn(Mono.empty());
        when(statusRepository.save(any(Status.class))).thenReturn(Mono.just(testStatus));
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(Mono.just(testLoanApplication));

        // When & Then
        StepVerifier.create(loanApplicationUseCase.create(testLoanApplication))
                .expectNext(testLoanApplication)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería actualizar solicitud exitosamente")
    void shouldUpdateLoanApplicationSuccessfully() {
        // Given
        when(loanApplicationRepository.findById(testId)).thenReturn(Mono.just(testLoanApplication));
        when(loanApplicationRepository.save(testLoanApplication)).thenReturn(Mono.just(testLoanApplication));

        // When & Then
        StepVerifier.create(loanApplicationUseCase.update(testLoanApplication))
                .expectNext(testLoanApplication)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería fallar al actualizar cuando la solicitud no existe")
    void shouldFailUpdateWhenApplicationNotExists() {
        // Given
        when(loanApplicationRepository.findById(testId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(loanApplicationUseCase.update(testLoanApplication))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Solicitud no encontrada")
                )
                .verify();
    }

    @Test
    @DisplayName("Debería obtener todas las solicitudes")
    void shouldGetAllLoanApplications() {
        // Given
        LoanApplication secondApplication = LoanApplication.builder()
                .id(UUID.randomUUID())
                .email("another@example.com")
                .amount(BigDecimal.valueOf(15000))
                .term(24)
                .statusId(2)
                .applicationTypeId(2)
                .build();

        when(loanApplicationRepository.findAll()).thenReturn(Flux.just(testLoanApplication, secondApplication));

        // When & Then
        StepVerifier.create(loanApplicationUseCase.getAll())
                .expectNext(testLoanApplication)
                .expectNext(secondApplication)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería obtener solicitud por ID")
    void shouldGetLoanApplicationById() {
        // Given
        when(loanApplicationRepository.findById(testId)).thenReturn(Mono.just(testLoanApplication));

        // When & Then
        StepVerifier.create(loanApplicationUseCase.getById(testId))
                .expectNext(testLoanApplication)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería retornar vacío cuando no encuentra solicitud por ID")
    void shouldReturnEmptyWhenNotFoundById() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(loanApplicationRepository.findById(nonExistentId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(loanApplicationUseCase.getById(nonExistentId))
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería eliminar solicitud exitosamente")
    void shouldDeleteLoanApplicationSuccessfully() {
        // Given
        when(loanApplicationRepository.deleteById(testId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(loanApplicationUseCase.delete(testId))
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería obtener resúmenes por estado con paginación")
    void shouldFindSummariesByStatusWithPagination() {
        // Given
        String statusName = "PENDING";
        int page = 0;
        int size = 10;

        when(loanApplicationRepository.findSummariesByStatus("PENDING", page, size))
                .thenReturn(Flux.just(testSummaryDTO));

        // When & Then
        StepVerifier.create(loanApplicationUseCase.findByStatusName(statusName, page, size))
                .expectNext(testSummaryDTO)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería convertir nombre de estado a mayúsculas")
    void shouldConvertStatusNameToUpperCase() {
        // Given
        String lowerCaseStatusName = "pending";
        when(loanApplicationRepository.findSummariesByStatus("PENDING", 0, 10))
                .thenReturn(Flux.just(testSummaryDTO));

        // When & Then
        StepVerifier.create(loanApplicationUseCase.findByStatusName(lowerCaseStatusName, 0, 10))
                .expectNext(testSummaryDTO)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería manejar error en verificación de usuario")
    void shouldHandleUserVerificationError() {
        // Given
        RuntimeException verificationError = new RuntimeException("User service unavailable");
        when(verifyEmailExists.userExistsByEmail(testEmail)).thenReturn(Mono.error(verificationError));

        // When & Then
        StepVerifier.create(loanApplicationUseCase.create(testLoanApplication))
                .expectError(RuntimeException.class)
                .verify();
    }
}
