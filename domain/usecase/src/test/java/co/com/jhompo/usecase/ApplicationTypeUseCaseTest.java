package co.com.jhompo.usecase;

import co.com.jhompo.model.applicationtype.ApplicationType;
import co.com.jhompo.model.applicationtype.gateways.ApplicationTypeRepository;
import co.com.jhompo.usecase.applicationtype.ApplicationTypeUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationTypeUseCaseTest {

    @Mock
    private ApplicationTypeRepository repository;

    private ApplicationTypeUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ApplicationTypeUseCase(repository);
    }

    @Test
    void create_ShouldSaveApplicationType_WhenValidApplicationTypeProvided() {
        // Given
        ApplicationType inputApplicationType = ApplicationType.builder()
                .name("Personal Loan")
                .minimum_amount(1000.0)
                .maximum_amount(50000.0)
                .interest_rate(12.5)
                .automatic_validation(true)
                .build();

        ApplicationType savedApplicationType = ApplicationType.builder()
                .id(1)
                .name("Personal Loan")
                .minimum_amount(1000.0)
                .maximum_amount(50000.0)
                .interest_rate(12.5)
                .automatic_validation(true)
                .build();

        when(repository.save(any(ApplicationType.class)))
                .thenReturn(Mono.just(savedApplicationType));

        // When & Then
        StepVerifier.create(useCase.create(inputApplicationType))
                .expectNext(savedApplicationType)
                .verifyComplete();

        verify(repository).save(inputApplicationType);
    }

    @Test
    void create_ShouldPropagateError_WhenRepositoryFails() {
        // Given
        ApplicationType inputApplicationType = ApplicationType.builder()
                .name("Business Loan")
                .minimum_amount(5000.0)
                .maximum_amount(100000.0)
                .interest_rate(15.0)
                .automatic_validation(false)
                .build();

        RuntimeException expectedError = new RuntimeException("Database error");
        when(repository.save(any(ApplicationType.class)))
                .thenReturn(Mono.error(expectedError));

        // When & Then
        StepVerifier.create(useCase.create(inputApplicationType))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void getApplicationTypeById_ShouldReturnApplicationType_WhenIdExists() {
        // Given
        Integer id = 1;
        ApplicationType expectedApplicationType = ApplicationType.builder()
                .id(id)
                .name("Mortgage Loan")
                .minimum_amount(20000.0)
                .maximum_amount(500000.0)
                .interest_rate(8.5)
                .automatic_validation(false)
                .build();

        when(repository.findById(id))
                .thenReturn(Mono.just(expectedApplicationType));

        // When & Then
        StepVerifier.create(useCase.getApplicationTypeById(id))
                .expectNext(expectedApplicationType)
                .verifyComplete();

        verify(repository).findById(id);
    }

    @Test
    void getApplicationTypeById_ShouldThrowException_WhenIdDoesNotExist() {
        // Given
        Integer id = 999;
        when(repository.findById(id))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(useCase.getApplicationTypeById(id))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Status with id " + id + " not found."))
                .verify();

        verify(repository).findById(id);
    }

    @Test
    void getAllStatuses_ShouldReturnAllApplicationTypes_WhenCalled() {
        // Given
        ApplicationType type1 = ApplicationType.builder()
                .id(1)
                .name("Personal Loan")
                .minimum_amount(1000.0)
                .maximum_amount(50000.0)
                .interest_rate(12.5)
                .automatic_validation(true)
                .build();

        ApplicationType type2 = ApplicationType.builder()
                .id(2)
                .name("Car Loan")
                .minimum_amount(5000.0)
                .maximum_amount(80000.0)
                .interest_rate(9.8)
                .automatic_validation(false)
                .build();

        when(repository.findAll())
                .thenReturn(Flux.just(type1, type2));

        // When & Then
        StepVerifier.create(useCase.getAllStatuses())
                .expectNext(type1)
                .expectNext(type2)
                .verifyComplete();

        verify(repository).findAll();
    }

    @Test
    void getAllStatuses_ShouldReturnEmpty_WhenNoApplicationTypesExist() {
        // Given
        when(repository.findAll())
                .thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(useCase.getAllStatuses())
                .verifyComplete();

        verify(repository).findAll();
    }

    @Test
    void updateStatus_ShouldUpdateApplicationType_WhenIdExists() {
        // Given
        ApplicationType existingApplicationType = ApplicationType.builder()
                .id(1)
                .name("Personal Loan")
                .minimum_amount(1000.0)
                .maximum_amount(50000.0)
                .interest_rate(12.5)
                .automatic_validation(false)
                .build();

        ApplicationType updatedApplicationType = ApplicationType.builder()
                .id(1)
                .name("Enhanced Personal Loan")
                .minimum_amount(1500.0)
                .maximum_amount(60000.0)
                .interest_rate(11.8)
                .automatic_validation(true)
                .build();

        when(repository.findById(1))
                .thenReturn(Mono.just(existingApplicationType));
        when(repository.save(updatedApplicationType))
                .thenReturn(Mono.just(updatedApplicationType));

        // When & Then
        StepVerifier.create(useCase.updateStatus(updatedApplicationType))
                .expectNext(updatedApplicationType)
                .verifyComplete();

        verify(repository).findById(1);
        verify(repository).save(updatedApplicationType);
    }

    @Test
    void updateStatus_ShouldThrowException_WhenIdDoesNotExist() {
        // Given
        ApplicationType nonExistentApplicationType = ApplicationType.builder()
                .id(999)
                .name("Non Existent Loan")
                .minimum_amount(1000.0)
                .maximum_amount(10000.0)
                .interest_rate(15.0)
                .automatic_validation(false)
                .build();

        when(repository.findById(999))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(useCase.updateStatus(nonExistentApplicationType))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Status with id " + 999 + " not found."))
                .verify();

        verify(repository).findById(999);
    }

    @Test
    void updateStatus_ShouldPropagateError_WhenSaveFails() {
        // Given
        ApplicationType existingApplicationType = ApplicationType.builder()
                .id(1)
                .name("Student Loan")
                .minimum_amount(500.0)
                .maximum_amount(30000.0)
                .interest_rate(6.5)
                .automatic_validation(true)
                .build();

        ApplicationType updatedApplicationType = ApplicationType.builder()
                .id(1)
                .name("Updated Student Loan")
                .minimum_amount(600.0)
                .maximum_amount(35000.0)
                .interest_rate(6.2)
                .automatic_validation(true)
                .build();

        RuntimeException saveError = new RuntimeException("Save failed");

        when(repository.findById(1))
                .thenReturn(Mono.just(existingApplicationType));
        when(repository.save(updatedApplicationType))
                .thenReturn(Mono.error(saveError));

        // When & Then
        StepVerifier.create(useCase.updateStatus(updatedApplicationType))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void deleteStatus_ShouldDeleteApplicationType_WhenIdProvided() {
        // Given
        Integer id = 1;
        when(repository.deleteById(id))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(useCase.deleteStatus(id))
                .verifyComplete();

        verify(repository).deleteById(id);
    }

    @Test
    void deleteStatus_ShouldPropagateError_WhenDeleteFails() {
        // Given
        Integer id = 1;
        RuntimeException deleteError = new RuntimeException("Delete failed");
        when(repository.deleteById(id))
                .thenReturn(Mono.error(deleteError));

        // When & Then
        StepVerifier.create(useCase.deleteStatus(id))
                .expectError(RuntimeException.class)
                .verify();

        verify(repository).deleteById(id);
    }
}