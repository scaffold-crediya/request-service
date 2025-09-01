package co.com.jhompo.api;

import co.com.jhompo.api.dtos.status.StatusDTO;
import co.com.jhompo.api.dtos.status.StatusRequestDTO;
import co.com.jhompo.api.mapper.StatusMapper;
import co.com.jhompo.model.status.Status;
import co.com.jhompo.usecase.status.StatusUseCase;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusControllerTest {

    @Mock
    private StatusUseCase statusUseCase;

    @Mock
    private StatusMapper statusMapper;

    @InjectMocks
    private StatusController statusController;

    private StatusRequestDTO testStatusRequestDTO;
    private StatusDTO testStatusDTO;
    private Status testStatus;
    private final Integer testId = 1;
    private final String testName = "PENDING";
    private final String testDescription = "Application is pending review";

    @BeforeEach
    void setUp() {
        testStatusRequestDTO = new StatusRequestDTO(testName, testDescription);

        testStatusDTO = new StatusDTO(testId, testName, testDescription);

        testStatus = Status.builder()
                .id(testId)
                .name(testName)
                .description(testDescription)
                .build();
    }

    @Test
    @DisplayName("Debería crear estado exitosamente")
    void shouldCreateStatusSuccessfully() {
        // Given
        when(statusMapper.toDomain(0, testStatusRequestDTO)).thenReturn(testStatus);
        when(statusUseCase.createStatus(any(Status.class))).thenReturn(Mono.just(testStatus));
        when(statusMapper.toDto(testStatus)).thenReturn(testStatusDTO);

        // When & Then
        StepVerifier.create(statusController.create(testStatusRequestDTO))
                .expectNext(testStatusDTO)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería obtener todos los estados exitosamente")
    void shouldGetAllStatusesSuccessfully() {
        // Given
        Status secondStatus = Status.builder()
                .id(2)
                .name("APPROVED")
                .description("Application is approved")
                .build();

        StatusDTO secondStatusDTO = new StatusDTO(2, "APPROVED", "Application is approved");

        when(statusUseCase.getAllStatuses()).thenReturn(Flux.just(testStatus, secondStatus));
        when(statusMapper.toDto(testStatus)).thenReturn(testStatusDTO);
        when(statusMapper.toDto(secondStatus)).thenReturn(secondStatusDTO);

        // When & Then
        StepVerifier.create(statusController.getAll())
                .expectNext(testStatusDTO)
                .expectNext(secondStatusDTO)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería obtener estado por ID exitosamente")
    void shouldGetStatusByIdSuccessfully() {
        // Given
        when(statusUseCase.getStatusById(testId)).thenReturn(Mono.just(testStatus));
        when(statusMapper.toDto(testStatus)).thenReturn(testStatusDTO);

        // When & Then
        StepVerifier.create(statusController.getById(testId))
                .expectNext(testStatusDTO)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería retornar vacío cuando no encuentra estado por ID")
    void shouldReturnEmptyWhenStatusNotFoundById() {
        // Given
        Integer nonExistentId = 999;
        when(statusUseCase.getStatusById(nonExistentId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(statusController.getById(nonExistentId))
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería actualizar estado exitosamente")
    void shouldUpdateStatusSuccessfully() {
        // Given
        StatusRequestDTO updateRequestDTO = new StatusRequestDTO("REJECTED", "Application is rejected");

        Status updatedStatus = Status.builder()
                .id(testId)
                .name("REJECTED")
                .description("Application is rejected")
                .build();

        StatusDTO updatedStatusDTO = new StatusDTO(testId, "REJECTED", "Application is rejected");

        when(statusMapper.toDomain(testId, updateRequestDTO)).thenReturn(updatedStatus);
        when(statusUseCase.updateStatus(updatedStatus)).thenReturn(Mono.just(updatedStatus));
        when(statusMapper.toDto(updatedStatus)).thenReturn(updatedStatusDTO);

        // When & Then
        StepVerifier.create(statusController.update(testId, updateRequestDTO))
                .expectNext(updatedStatusDTO)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería eliminar estado exitosamente")
    void shouldDeleteStatusSuccessfully() {
        // Given
        when(statusUseCase.deleteStatus(testId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(statusController.delete(testId))
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería manejar error del caso de uso en creación")
    void shouldHandleUseCaseErrorOnCreate() {
        // Given
        RuntimeException expectedError = new RuntimeException("Database error");
        when(statusMapper.toDomain(0, testStatusRequestDTO)).thenReturn(testStatus);
        when(statusUseCase.createStatus(any(Status.class))).thenReturn(Mono.error(expectedError));

        // When & Then
        StepVerifier.create(statusController.create(testStatusRequestDTO))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Debería manejar error del caso de uso en consulta por ID")
    void shouldHandleUseCaseErrorOnGetById() {
        // Given
        RuntimeException expectedError = new RuntimeException("Database connection failed");
        when(statusUseCase.getStatusById(testId)).thenReturn(Mono.error(expectedError));

        // When & Then
        StepVerifier.create(statusController.getById(testId))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Debería manejar error del caso de uso en actualización")
    void shouldHandleUseCaseErrorOnUpdate() {
        // Given
        RuntimeException expectedError = new RuntimeException("Update failed");
        when(statusMapper.toDomain(testId, testStatusRequestDTO)).thenReturn(testStatus);
        when(statusUseCase.updateStatus(any(Status.class))).thenReturn(Mono.error(expectedError));

        // When & Then
        StepVerifier.create(statusController.update(testId, testStatusRequestDTO))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Debería manejar error del caso de uso en eliminación")
    void shouldHandleUseCaseErrorOnDelete() {
        // Given
        RuntimeException expectedError = new RuntimeException("Delete failed");
        when(statusUseCase.deleteStatus(testId)).thenReturn(Mono.error(expectedError));

        // When & Then
        StepVerifier.create(statusController.delete(testId))
                .expectError(RuntimeException.class)
                .verify();
    }



    @Test
    @DisplayName("Debería manejar múltiples estados vacíos")
    void shouldHandleEmptyStatusList() {
        // Given
        when(statusUseCase.getAllStatuses()).thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(statusController.getAll())
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería usar placeholder ID correcto en creación")
    void shouldUsePlaceholderIdInCreation() {
        // Given
        Status statusWithPlaceholderId = Status.builder()
                .id(0) // Placeholder ID
                .name(testName)
                .description(testDescription)
                .build();

        Status createdStatus = Status.builder()
                .id(5) // ID generado por DB
                .name(testName)
                .description(testDescription)
                .build();

        StatusDTO createdStatusDTO = new StatusDTO(5, testName, testDescription);

        when(statusMapper.toDomain(0, testStatusRequestDTO)).thenReturn(statusWithPlaceholderId);
        when(statusUseCase.createStatus(statusWithPlaceholderId)).thenReturn(Mono.just(createdStatus));
        when(statusMapper.toDto(createdStatus)).thenReturn(createdStatusDTO);

        // When & Then
        StepVerifier.create(statusController.create(testStatusRequestDTO))
                .expectNext(createdStatusDTO)
                .verifyComplete();
    }
}