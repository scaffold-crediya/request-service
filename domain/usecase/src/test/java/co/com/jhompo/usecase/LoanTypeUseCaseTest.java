package co.com.jhompo.usecase;

import co.com.jhompo.model.loantype.LoanType;
import co.com.jhompo.model.loantype.gateways.LoanTypeRepository;
import co.com.jhompo.usecase.loantype.LoanTypeUseCase;
import co.com.jhompo.util.Messages.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanTypeUseCaseTest {

    @Mock
    private LoanTypeRepository repository;

    private LoanTypeUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new LoanTypeUseCase(repository);
    }

    @Test
    void create_ShouldSaveApplicationType_WhenValidApplicationTypeProvided() {
        // Given
        LoanType inputLoanType = LoanType.builder()
                .name("Personal Loan")
                .minimum_amount(BigDecimal.valueOf(1000.0))
                .maximum_amount(BigDecimal.valueOf(50000.0))
                .interest_rate(12.5)
                .automatic_validation(true)
                .build();

        LoanType savedLoanType = LoanType.builder()
                .id(1)
                .name("Personal Loan")
                .minimum_amount(BigDecimal.valueOf(1000.0))
                .maximum_amount(BigDecimal.valueOf(50000.0))
                .interest_rate(12.5)
                .automatic_validation(true)
                .build();

        when(repository.save(any(LoanType.class)))
                .thenReturn(Mono.just(savedLoanType));

        // When & Then
        StepVerifier.create(useCase.create(inputLoanType))
                .expectNext(savedLoanType)
                .verifyComplete();

        verify(repository).save(inputLoanType);
    }

    @Test
    void create_ShouldPropagateError_WhenRepositoryFails() {
        // Given
        LoanType inputLoanType = LoanType.builder()
                .name("Business Loan")
                .minimum_amount(BigDecimal.valueOf(5000.0))
                .maximum_amount(BigDecimal.valueOf(100000.0))
                .interest_rate(15.0)
                .automatic_validation(false)
                .build();

        RuntimeException expectedError = new RuntimeException("Database error");
        when(repository.save(any(LoanType.class)))
                .thenReturn(Mono.error(expectedError));

        // When & Then
        StepVerifier.create(useCase.create(inputLoanType))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void getApplicationTypeById_ShouldReturnApplicationType_WhenIdExists() {
        // Given
        Integer id = 1;
        LoanType expectedLoanType = LoanType.builder()
                .id(id)
                .name("Mortgage Loan")
                .minimum_amount(BigDecimal.valueOf(20000.0))
                .maximum_amount(BigDecimal.valueOf(500000.0))
                .interest_rate(8.5)
                .automatic_validation(false)
                .build();

        when(repository.findById(id))
                .thenReturn(Mono.just(expectedLoanType));

        // When & Then
        StepVerifier.create(useCase.getApplicationTypeById(id))
                .expectNext(expectedLoanType)
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
                                throwable.getMessage().equals(APPLICATION_TYPE.NOT_FOUND))
                .verify();

        verify(repository).findById(id);
    }

    @Test
    void getAllStatuses_ShouldReturnAllApplicationTypes_WhenCalled() {
        // Given
        LoanType type1 = LoanType.builder()
                .id(1)
                .name("Personal Loan")
                .minimum_amount(BigDecimal.valueOf(1000.0))
                .maximum_amount(BigDecimal.valueOf(50000.0))
                .interest_rate(12.5)
                .automatic_validation(true)
                .build();

        LoanType type2 = LoanType.builder()
                .id(2)
                .name("Car Loan")
                .minimum_amount(BigDecimal.valueOf(5000.0))
                .maximum_amount(BigDecimal.valueOf(80000.0))
                .interest_rate(9.8)
                .automatic_validation(false)
                .build();

        when(repository.findAll())
                .thenReturn(Flux.just(type1, type2));

        // When & Then
        StepVerifier.create(useCase.getAllType())
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
        StepVerifier.create(useCase.getAllType())
                .verifyComplete();

        verify(repository).findAll();
    }

    @Test
    void updateStatus_ShouldUpdateApplicationType_WhenIdExists() {
        // Given
        LoanType existingLoanType = LoanType.builder()
                .id(1)
                .name("Personal Loan")
                .minimum_amount(BigDecimal.valueOf(1000.0))
                .maximum_amount(BigDecimal.valueOf(50000.0))
                .interest_rate(12.5)
                .automatic_validation(false)
                .build();

        LoanType updatedLoanType = LoanType.builder()
                .id(1)
                .name("Enhanced Personal Loan")
                .minimum_amount(BigDecimal.valueOf(1500.0))
                .maximum_amount(BigDecimal.valueOf(60000.0))
                .interest_rate(11.8)
                .automatic_validation(true)
                .build();

        when(repository.findById(1))
                .thenReturn(Mono.just(existingLoanType));
        when(repository.save(updatedLoanType))
                .thenReturn(Mono.just(updatedLoanType));

        // When & Then
        StepVerifier.create(useCase.updateLoanType(updatedLoanType))
                .expectNext(updatedLoanType)
                .verifyComplete();

        verify(repository).findById(1);
        verify(repository).save(updatedLoanType);
    }

    @Test
    void updateStatus_ShouldThrowException_WhenIdDoesNotExist() {
        // Given
        LoanType nonExistentLoanType = LoanType.builder()
                .id(999)
                .name("Non Existent Loan")
                .minimum_amount(BigDecimal.valueOf(1000.0))
                .maximum_amount(BigDecimal.valueOf(10000.0))
                .interest_rate(15.0)
                .automatic_validation(false)
                .build();

        when(repository.findById(999))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(useCase.updateLoanType(nonExistentLoanType))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals(APPLICATION_TYPE.NOT_FOUND))
                .verify();

        verify(repository).findById(999);
    }

    @Test
    void updateStatus_ShouldPropagateError_WhenSaveFails() {
        // Given
        LoanType existingLoanType = LoanType.builder()
                .id(1)
                .name("Student Loan")
                .minimum_amount(BigDecimal.valueOf(500.0))
                .maximum_amount(BigDecimal.valueOf(30000.0))
                .interest_rate(6.5)
                .automatic_validation(true)
                .build();

        LoanType updatedLoanType = LoanType.builder()
                .id(1)
                .name("Updated Student Loan")
                .minimum_amount(BigDecimal.valueOf(600.0))
                .maximum_amount(BigDecimal.valueOf(35000.0))
                .interest_rate(6.2)
                .automatic_validation(true)
                .build();

        RuntimeException saveError = new RuntimeException("Save failed");

        when(repository.findById(1))
                .thenReturn(Mono.just(existingLoanType));
        when(repository.save(updatedLoanType))
                .thenReturn(Mono.error(saveError));

        // When & Then
        StepVerifier.create(useCase.updateLoanType(updatedLoanType))
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
        StepVerifier.create(useCase.deleteLoanType(id))
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
        StepVerifier.create(useCase.deleteLoanType(id))
                .expectError(RuntimeException.class)
                .verify();

        verify(repository).deleteById(id);
    }
}