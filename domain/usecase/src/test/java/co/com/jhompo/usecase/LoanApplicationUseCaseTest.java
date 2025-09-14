package co.com.jhompo.usecase;

import co.com.jhompo.model.gateways.NotificationGateway;
import co.com.jhompo.model.loanapplication.dto.LoanValidation;
import co.com.jhompo.model.loantype.LoanType;
import co.com.jhompo.model.loantype.gateways.LoanTypeRepository;
import co.com.jhompo.model.loanapplication.LoanApplication;
import co.com.jhompo.model.loanapplication.dto.LoanApplicationSummaryDTO;
import co.com.jhompo.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.jhompo.model.user.User;
import co.com.jhompo.model.user.gateways.UserExistenceGateway;
import co.com.jhompo.model.status.Status;
import co.com.jhompo.model.status.gateways.StatusRepository;
import co.com.jhompo.usecase.loanapplication.LoanApplicationUseCase;
import co.com.jhompo.util.Messages.*;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanApplicationUseCaseTest {

    @Mock
    private UserExistenceGateway verifyEmailExists;

    @Mock
    private NotificationGateway notificationGateway;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private LoanTypeRepository loanTypeRepository;

    @Mock
    private StatusRepository statusRepository;

    @InjectMocks
    private LoanApplicationUseCase loanApplicationUseCase;

    private LoanApplication testLoanApplication;
    private LoanType testLoanType;
    private Status testStatus;
    private User testUser;
    private LoanApplicationSummaryDTO testSummaryDTO;
    private final UUID testId = UUID.randomUUID();
    private final String testEmail = "test@example.com";
    private final String statusName = "PENDIENTE_REVISION";

    @BeforeEach
    void setUp() {
        loanApplicationUseCase = new LoanApplicationUseCase(
                loanApplicationRepository,
                loanTypeRepository,
                statusRepository,
                verifyEmailExists,
                notificationGateway
        );

        testLoanApplication = LoanApplication.builder()
                .id(testId)
                .email(testEmail)
                .amount(BigDecimal.valueOf(10000))
                .term(12)
                .statusId(1)
                .applicationTypeId(1)
                .build();

        testLoanType = LoanType.builder()
                .id(1)
                .name("Personal Loan")
                .minimum_amount(BigDecimal.valueOf(1000.0))
                .maximum_amount(BigDecimal.valueOf(50000.0))
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

        testUser = User.builder()
                .email(testEmail)
                .firstName("Jean Carlos")
                .baseSalary(BigDecimal.valueOf(5000))
                .build();
    }


    @Test
    @DisplayName("Deberia fallar cuando el usuario no existe")
    void shouldFailWhenUserDoesNotExist() {
        // Given
        when(verifyEmailExists.userExistsByEmail(testEmail)).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(loanApplicationUseCase.create(testLoanApplication))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains(LOAN_APPLICATION.EMAIL_NOT_FOUND)
                )
                .verify();
    }

    @Test
    @DisplayName("Deberia fallar cuando el tipo de aplicación no existe")
    void shouldFailWhenApplicationTypeDoesNotExist() {
        // Given
        when(verifyEmailExists.userExistsByEmail(testEmail)).thenReturn(Mono.just(true));
        when(loanTypeRepository.findById(1)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(loanApplicationUseCase.create(testLoanApplication))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().equals("Solicitud de prestamo no encontrada")
                )
                .verify();
    }



    @Test
    @DisplayName("Deberia actualizar solicitud exitosamente")
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
    @DisplayName("Deberia fallar al actualizar cuando la solicitud no existe")
    void shouldFailUpdateWhenApplicationNotExists() {
        // Given
        when(loanApplicationRepository.findById(testId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(loanApplicationUseCase.update(testLoanApplication))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Solicitud de prestamo no encontrada")
                )
                .verify();
    }

    @Test
    @DisplayName("Deberia obtener todas las solicitudes")
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
    @DisplayName("Deberia obtener solicitud por ID")
    void shouldGetLoanApplicationById() {
        // Given
        when(loanApplicationRepository.findById(testId)).thenReturn(Mono.just(testLoanApplication));

        // When & Then
        StepVerifier.create(loanApplicationUseCase.getById(testId))
                .expectNext(testLoanApplication)
                .verifyComplete();
    }

    @Test
    @DisplayName("Deberia retornar vacio cuando no encuentra solicitud por ID")
    void shouldReturnEmptyWhenNotFoundById() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(loanApplicationRepository.findById(nonExistentId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(loanApplicationUseCase.getById(nonExistentId))
                .verifyComplete();
    }

    @Test
    @DisplayName("Deberia eliminar solicitud exitosamente")
    void shouldDeleteLoanApplicationSuccessfully() {
        // Given
        when(loanApplicationRepository.deleteById(testId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(loanApplicationUseCase.delete(testId))
                .verifyComplete();
    }

    @Test
    @DisplayName("Deberia obtener resúmenes por estado con paginación")
    void shouldFindSummariesByStatusWithPagination() {
        // Given
        String statusName = "PENDING";
        int page = 0;
        int size = 10;

        // Configura el mock para que devuelva el DTO
        when(loanApplicationRepository.findSummariesByStatus("PENDING", page, size))
                .thenReturn(Flux.just(testSummaryDTO));

        // Aquí, se simula que el usuario existe.
        when(verifyEmailExists.findUserDetailsByEmails(any(List.class)))
                .thenReturn(Flux.just(
                        User.builder()
                                .email("test@example.com")
                                .firstName("Test User")
                                .baseSalary(BigDecimal.valueOf(5000))
                                .build()
                ));

        // When & Then
        StepVerifier.create(loanApplicationUseCase.findByStatusName(statusName, page, size))
                .expectNext(testSummaryDTO)
                .verifyComplete();
    }

    @Test
    @DisplayName("Deberia convertir nombre de estado a mayusculas")
    void shouldConvertStatusNameToUpperCase() {
        // Given
        String lowerCaseStatusName = "pending";

        // Mockea el resultado de la busqueda de resumenes
        when(loanApplicationRepository.findSummariesByStatus("PENDING", 0, 10))
                .thenReturn(Flux.just(testSummaryDTO));

        // Agrega esta linea: mockea el comportamiento del UserExistenceGateway
        // para que devuelva un Flux vacio en lugar de null.
        when(verifyEmailExists.findUserDetailsByEmails(any(List.class)))
                .thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(loanApplicationUseCase.findByStatusName(lowerCaseStatusName, 0, 10))
                .expectNext(testSummaryDTO)
                .verifyComplete();
    }

    @Test
    @DisplayName("Deberia manejar error en verificación de usuario")
    void shouldHandleUserVerificationError() {
        // Given
        RuntimeException verificationError = new RuntimeException("User service unavailable");
        when(verifyEmailExists.userExistsByEmail(testEmail)).thenReturn(Mono.error(verificationError));

        // When & Then
        StepVerifier.create(loanApplicationUseCase.create(testLoanApplication))
                .expectError(RuntimeException.class)
                .verify();
    }


    @Test
    @DisplayName("Deberia fallar cuando el monto excede el maximo permitido")
    void shouldFailWhenAmountExceedsMaximum() {
        // Given
        LoanApplication tooBigLoan = testLoanApplication;
        tooBigLoan.setAmount(BigDecimal.valueOf(1000000));

        when(verifyEmailExists.userExistsByEmail(testEmail)).thenReturn(Mono.just(true));
        when(loanTypeRepository.findById(1)).thenReturn(Mono.just(testLoanType));

        // When & Then
        StepVerifier.create(loanApplicationUseCase.create(tooBigLoan))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("fuera del rango permitido")
                )
                .verify();
    }


    @Test
    @DisplayName("Deberia enviar a validacion manual cuando el tipo de prestamo no es automatico")
    void shouldSendToManualValidationWhenTypeIsNotAutomatic() {
        // Given
        testLoanType = testLoanType.toBuilder()
                .automatic_validation(false)
                .build();

        when(verifyEmailExists.userExistsByEmail(testEmail)).thenReturn(Mono.just(true));
        when(loanTypeRepository.findById(1)).thenReturn(Mono.just(testLoanType));
        when(statusRepository.findByName("PENDIENTE_REVISION")).thenReturn(Mono.just(testStatus));
        when(loanApplicationRepository.save(any(LoanApplication.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // When & Then
        StepVerifier.create(loanApplicationUseCase.create(testLoanApplication))
                .expectNextMatches(app -> app.getStatusId() == testStatus.getId())
                .verifyComplete();
    }


    // ============= PRUEBAS PARA updateStatusAndGetDetails =============

    @Test
    @DisplayName("Deberia actualizar estado y obtener detalles exitosamente con validacion automatica")
    void shouldUpdateStatusAndGetDetailsWithAutomaticValidation() {
        // Given
        Integer newStatusId = 2;
        Status approvedStatus = Status.builder().id(3).name("APROBADO").build();

        when(loanApplicationRepository.findById(testId)).thenReturn(Mono.just(testLoanApplication));
        when(loanTypeRepository.findById(1)).thenReturn(Mono.just(testLoanType));
        when(verifyEmailExists.findUserDetailsByEmails(List.of(testEmail))).thenReturn(Flux.just(testUser));
        when(statusRepository.findByName("APROBADO")).thenReturn(Mono.just(approvedStatus));
        when(loanApplicationRepository.findByEmailAndStatusId(testEmail, 3)).thenReturn(Flux.empty());
        when(notificationGateway.sendForValidation(any(LoanValidation.class))).thenReturn(Mono.empty());
        when(statusRepository.findById(1)).thenReturn(Mono.just(testStatus));

        // When & Then
        StepVerifier.create(loanApplicationUseCase.updateStatusAndGetDetails(testId, newStatusId))
                .expectNextMatches(tuple ->
                        tuple.getT1().getId().equals(testId) &&
                                tuple.getT2().getId() == 1 &&
                                tuple.getT3().getId() == 1
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Deberia actualizar estado y obtener detalles con validacion manual")
    void shouldUpdateStatusAndGetDetailsWithManualValidation() {
        // Given
        Integer newStatusId = 2;
        LoanType manualType = testLoanType.toBuilder().automatic_validation(false).build();
        Status newStatus = Status.builder().id(2).name("APROBADO").build();

        when(loanApplicationRepository.findById(testId)).thenReturn(Mono.just(testLoanApplication));
        when(loanTypeRepository.findById(1)).thenReturn(Mono.just(manualType));
        when(loanApplicationRepository.save(any(LoanApplication.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(statusRepository.findById(newStatusId)).thenReturn(Mono.just(newStatus));
        when(notificationGateway.sendNotification(anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(loanApplicationUseCase.updateStatusAndGetDetails(testId, newStatusId))
                .expectNextMatches(tuple ->
                        tuple.getT1().getStatusId().equals(newStatusId) &&
                                tuple.getT2().getName().equals("APROBADO") &&
                                tuple.getT3().getId() == 1
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Deberia fallar cuando no encuentra la aplicacion en updateStatusAndGetDetails")
    void shouldFailWhenApplicationNotFoundInUpdateStatus() {
        // Given
        when(loanApplicationRepository.findById(testId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(loanApplicationUseCase.updateStatusAndGetDetails(testId, 2))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().contains("Loan application not found")
                )
                .verify();
    }

// ============= PRUEBAS PARA getActiveDebts =============

    @Test
    @DisplayName("Deberia obtener deudas activas correctamente")
    void shouldGetActiveDebtsSuccessfully() {
        // Given
        Status approvedStatus = Status.builder().id(3).name("APROBADO").build();
        LoanApplication activeLoan = LoanApplication.builder()
                .id(UUID.randomUUID())
                .email(testEmail)
                .amount(BigDecimal.valueOf(20000))
                .term(24)
                .statusId(3)
                .applicationTypeId(1)
                .build();

        when(statusRepository.findByName("APROBADO")).thenReturn(Mono.just(approvedStatus));
        when(loanApplicationRepository.findByEmailAndStatusId(testEmail, 3))
                .thenReturn(Flux.just(activeLoan));
        when(loanTypeRepository.findById(1)).thenReturn(Mono.just(testLoanType));

        // When & Then
        StepVerifier.create(loanApplicationUseCase.getActiveDebts(testUser))
                .expectNextMatches(debts -> !debts.isEmpty())
                .verifyComplete();
    }

    @Test
    @DisplayName("Deberia retornar lista vacia cuando no hay deudas activas")
    void shouldReturnEmptyListWhenNoActiveDebts() {
        // Given
        Status approvedStatus = Status.builder().id(3).name("APROBADO").build();

        when(statusRepository.findByName("APROBADO")).thenReturn(Mono.just(approvedStatus));
        when(loanApplicationRepository.findByEmailAndStatusId(testEmail, 3))
                .thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(loanApplicationUseCase.getActiveDebts(testUser))
                .expectNext(List.of())
                .verifyComplete();
    }

    // ============= PRUEBAS PARA getDebtsInfo =============

    @Test
    @DisplayName("Deberia obtener informacion de deudas exitosamente")
    void shouldGetDebtsInfoSuccessfully() {
        // Given
        Status approvedStatus = Status.builder().id(3).name("APROBADO").build();
        LoanApplication activeLoan1 = LoanApplication.builder()
                .id(UUID.randomUUID())
                .email(testEmail)
                .amount(BigDecimal.valueOf(15000))
                .term(12)
                .statusId(3)
                .applicationTypeId(1)
                .build();

        LoanApplication activeLoan2 = LoanApplication.builder()
                .id(UUID.randomUUID())
                .email(testEmail)
                .amount(BigDecimal.valueOf(25000))
                .term(24)
                .statusId(3)
                .applicationTypeId(1)
                .build();

        when(statusRepository.findByName("APROBADO")).thenReturn(Mono.just(approvedStatus));
        when(loanApplicationRepository.findByEmailAndStatusId(testEmail, 3))
                .thenReturn(Flux.just(activeLoan1, activeLoan2));
        when(loanTypeRepository.findById(1)).thenReturn(Mono.just(testLoanType));

        // When & Then
        StepVerifier.create(loanApplicationUseCase.getDebtsInfo(testUser))
                .expectNextMatches(tuple ->
                        tuple.getT1().size() == 2 && // 2 pagos mensuales
                                tuple.getT2() == 40000.0 // deuda total
                )
                .verifyComplete();
    }

    // ============= PRUEBAS PARA handleAutomaticValidation =============

    @Test
    @DisplayName("Deberia lanzar una excepcion si la aplicacion de prestamo no se encuentra")
    void shouldThrowExceptionWhenLoanApplicationNotFound() {
        // Given
        // Mockea el repositorio para que devuelva un Mono vacío, simulando que no se encuentra nada
        when(loanApplicationRepository.findById(testId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(loanApplicationUseCase.updateStatusAndGetDetails(testId, 2))
                // Ahora sí, espera el error con el mensaje específico
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().contains("Loan application not found")
                )
                .verify();
    }

    @Test
    @DisplayName("Deberia fallar validacion automatica cuando usuario no existe")
    void shouldFailAutomaticValidationWhenUserNotFound() {
        // Given
        when(loanApplicationRepository.findById(testId)).thenReturn(Mono.just(testLoanApplication));
        when(loanTypeRepository.findById(1)).thenReturn(Mono.just(testLoanType));
        when(verifyEmailExists.findUserDetailsByEmails(List.of(testEmail))).thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(loanApplicationUseCase.updateStatusAndGetDetails(testId, 2))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().contains("User not found for email")
                )
                .verify();
    }

    // ============= PRUEBAS PARA handleManualValidation =============

    @Test
    @DisplayName("Deberia manejar validacion manual exitosamente")
    void shouldHandleManualValidationSuccessfully() {
        // Given
        Integer newStatusId = 2;
        LoanType manualType = testLoanType.toBuilder().automatic_validation(false).build();
        Status newStatus = Status.builder().id(2).name("EN_REVISION").build();
        LoanApplication updatedApp = testLoanApplication;
        updatedApp.setStatusId(newStatusId);

        when(loanApplicationRepository.findById(testId)).thenReturn(Mono.just(testLoanApplication));
        when(loanTypeRepository.findById(1)).thenReturn(Mono.just(manualType));
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(Mono.just(updatedApp));
        when(statusRepository.findById(newStatusId)).thenReturn(Mono.just(newStatus));
        when(notificationGateway.sendNotification(anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(loanApplicationUseCase.updateStatusAndGetDetails(testId, newStatusId))
                .expectNextMatches(tuple ->
                        tuple.getT1().getStatusId().equals(newStatusId) &&
                                tuple.getT2().getName().equals("EN_REVISION")
                )
                .verifyComplete();
    }

    // ============= PRUEBA PARA CREACION CON VALIDACION AUTOMATICA =============




    // ============= PRUEBA PARA VALIDACION DE MONTO MINIMO =============

    @Test
    @DisplayName("Deberia fallar cuando el monto es menor al minimo permitido")
    void shouldFailWhenAmountIsBelowMinimum() {
        // Given
        LoanApplication tooSmallLoan = testLoanApplication;
        tooSmallLoan.setAmount(BigDecimal.valueOf(500));

        when(verifyEmailExists.userExistsByEmail(testEmail)).thenReturn(Mono.just(true));
        when(loanTypeRepository.findById(1)).thenReturn(Mono.just(testLoanType));

        // When & Then
        StepVerifier.create(loanApplicationUseCase.create(tooSmallLoan))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("fuera del rango permitido")
                )
                .verify();
    }


}
