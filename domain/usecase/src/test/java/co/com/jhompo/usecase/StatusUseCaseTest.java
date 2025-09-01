package co.com.jhompo.usecase;

import co.com.jhompo.model.status.Status;
import co.com.jhompo.model.status.gateways.StatusRepository;
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
class StatusUseCaseTest {

    @Mock
    private StatusRepository statusRepository;

    @InjectMocks
    private StatusUseCase statusUseCase;

    private Status testStatus;
    private final Integer testId = 1;
    private final String testName = "PENDING";
    private final String testDescription = "Application is pending review";

    @BeforeEach
    void setUp() {
        testStatus = Status.builder()
                .id(testId)
                .name(testName)
                .description(testDescription)
                .build();
    }

    @Test
    @DisplayName("Debería crear estado exitosamente cuando no existe duplicado")
    void shouldCreateStatusSuccessfully() {
        // Given
        when(statusRepository.findByName(testName)).thenReturn(Mono.empty());
        when(statusRepository.save(testStatus)).thenReturn(Mono.just(testStatus));

        // When & Then
        StepVerifier.create(statusUseCase.createStatus(testStatus))
                .expectNext(testStatus)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería fallar al crear estado cuando ya existe con el mismo nombre")
    void shouldFailToCreateStatusWhenNameAlreadyExists() {
        // Given
        Status existingStatus = Status.builder()
                .id(2)
                .name(testName)
                .description("Another description")
                .build();

        when(statusRepository.findByName(testName)).thenReturn(Mono.just(existingStatus));

        // When & Then
        StepVerifier.create(statusUseCase.createStatus(testStatus))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().equals("Status with name 'PENDING' already exists.")
                )
                .verify();
    }

    @Test
    @DisplayName("Debería obtener estado por ID exitosamente")
    void shouldGetStatusByIdSuccessfully() {
        // Given
        when(statusRepository.findById(testId)).thenReturn(Mono.just(testStatus));

        // When & Then
        StepVerifier.create(statusUseCase.getStatusById(testId))
                .expectNext(testStatus)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería fallar al obtener estado cuando no existe")
    void shouldFailToGetStatusWhenNotFound() {
        // Given
        Integer nonExistentId = 999;
        when(statusRepository.findById(nonExistentId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(statusUseCase.getStatusById(nonExistentId))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Status with id 999 not found.")
                )
                .verify();
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

        when(statusRepository.findAll()).thenReturn(Flux.just(testStatus, secondStatus));

        // When & Then
        StepVerifier.create(statusUseCase.getAllStatuses())
                .expectNext(testStatus)
                .expectNext(secondStatus)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería retornar flujo vacío cuando no hay estados")
    void shouldReturnEmptyFluxWhenNoStatuses() {
        // Given
        when(statusRepository.findAll()).thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(statusUseCase.getAllStatuses())
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería actualizar estado exitosamente cuando existe")
    void shouldUpdateStatusSuccessfully() {
        // Given
        Status updatedStatus = Status.builder()
                .id(testId)
                .name("UPDATED")
                .description("Updated description")
                .build();

        when(statusRepository.findById(testId)).thenReturn(Mono.just(testStatus));
        when(statusRepository.save(updatedStatus)).thenReturn(Mono.just(updatedStatus));

        // When & Then
        StepVerifier.create(statusUseCase.updateStatus(updatedStatus))
                .expectNext(updatedStatus)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería fallar al actualizar estado cuando no existe")
    void shouldFailToUpdateStatusWhenNotFound() {
        // Given
        Status updateStatus = Status.builder()
                .id(999)
                .name("NONEXISTENT")
                .description("Does not exist")
                .build();

        when(statusRepository.findById(999)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(statusUseCase.updateStatus(updateStatus))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Status with id 999 not found.")
                )
                .verify();
    }

    @Test
    @DisplayName("Debería eliminar estado exitosamente")
    void shouldDeleteStatusSuccessfully() {
        // Given
        when(statusRepository.deleteById(testId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(statusUseCase.deleteStatus(testId))
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería manejar error del repositorio en creación")
    void shouldHandleRepositoryErrorOnCreate() {
        // Given
        RuntimeException repositoryError = new RuntimeException("Database error");
        when(statusRepository.findByName(testName)).thenReturn(Mono.empty());
        when(statusRepository.save(any(Status.class))).thenReturn(Mono.error(repositoryError));

        // When & Then
        StepVerifier.create(statusUseCase.createStatus(testStatus))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Debería manejar error del repositorio en consulta")
    void shouldHandleRepositoryErrorOnFindById() {
        // Given
        RuntimeException repositoryError = new RuntimeException("Connection failed");
        when(statusRepository.findById(testId)).thenReturn(Mono.error(repositoryError));

        // When & Then
        StepVerifier.create(statusUseCase.getStatusById(testId))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Debería manejar error del repositorio en actualización")
    void shouldHandleRepositoryErrorOnUpdate() {
        // Given
        RuntimeException repositoryError = new RuntimeException("Update failed");
        when(statusRepository.findById(testId)).thenReturn(Mono.just(testStatus));
        when(statusRepository.save(any(Status.class))).thenReturn(Mono.error(repositoryError));

        // When & Then
        StepVerifier.create(statusUseCase.updateStatus(testStatus))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Debería validar regla de negocio de nombres únicos")
    void shouldValidateUniqueNameBusinessRule() {
        // Given
        Status duplicateNameStatus = Status.builder()
                .id(3)
                .name(testName) // Mismo nombre que testStatus
                .description("Different description")
                .build();

        when(statusRepository.findByName(testName)).thenReturn(Mono.just(testStatus));

        // When & Then
        StepVerifier.create(statusUseCase.createStatus(duplicateNameStatus))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("already exists")
                )
                .verify();
    }
}
