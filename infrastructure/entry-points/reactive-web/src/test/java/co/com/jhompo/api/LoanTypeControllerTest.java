package co.com.jhompo.api;

import co.com.jhompo.api.dtos.ApplicationTypeDTO;
import co.com.jhompo.api.mapper.ApplicationTypeMapper;
import co.com.jhompo.model.loantype.LoanType;
import co.com.jhompo.usecase.loantype.LoanTypeUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanTypeControllerTest {

    @Mock
    private LoanTypeUseCase service;

    @Mock
    private ApplicationTypeMapper mapper;

    @InjectMocks
    private LoanTypeController loanTypeController;

    private ApplicationTypeDTO testApplicationTypeDTO;
    private LoanType testLoanType;
    private final int testId = 1;
    private final String testName = "Personal Loan";
    private final BigDecimal testMinimumAmount = BigDecimal.valueOf(1000.0);
    private final BigDecimal testMaximumAmount = new BigDecimal("50000.0");
    private final double testInterestRate = 15.5;
    private final boolean testAutomaticValidation = true;

    @BeforeEach
    void setUp() {
        testApplicationTypeDTO = new ApplicationTypeDTO(testId, testName, testMinimumAmount,
                testMaximumAmount, testInterestRate, testAutomaticValidation);

        testLoanType = LoanType.builder()
                .id(testId)
                .name(testName)
                .minimum_amount(testMinimumAmount)
                .maximum_amount(testMaximumAmount)
                .interest_rate(testInterestRate)
                .automatic_validation(testAutomaticValidation)
                .build();
    }

    @Test
    @DisplayName("Debería crear tipo de aplicación exitosamente")
    void shouldCreateApplicationTypeSuccessfully() {
        // Given
        LoanType createdLoanType = LoanType.builder()
                .id(5) // ID generado por DB
                .name(testName)
                .minimum_amount(testMinimumAmount)
                .maximum_amount(testMaximumAmount)
                .interest_rate(testInterestRate)
                .automatic_validation(testAutomaticValidation)
                .build();

        ApplicationTypeDTO createdApplicationTypeDTO = new ApplicationTypeDTO(5, testName,
                testMinimumAmount, testMaximumAmount, testInterestRate, testAutomaticValidation);

        when(mapper.toEntity(testApplicationTypeDTO)).thenReturn(testLoanType);
        when(service.create(testLoanType)).thenReturn(Mono.just(createdLoanType));
        when(mapper.toDto(createdLoanType)).thenReturn(createdApplicationTypeDTO);

        // When & Then
        StepVerifier.create(loanTypeController.createApplicationType(testApplicationTypeDTO))
                .expectNext(new ResponseEntity<>(createdApplicationTypeDTO, HttpStatus.CREATED))
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería obtener todos los tipos de aplicación exitosamente")
    void shouldGetAllApplicationTypesSuccessfully() {
        // Given
        LoanType secondLoanType = LoanType.builder()
                .id(2)
                .name("Business Loan")
                .minimum_amount(BigDecimal.valueOf(5000.0))
                .maximum_amount(BigDecimal.valueOf(200000.0))
                .interest_rate(12.0)
                .automatic_validation(false)
                .build();

        ApplicationTypeDTO secondApplicationTypeDTO = new ApplicationTypeDTO(2, "Business Loan",
                BigDecimal.valueOf(5000.0), BigDecimal.valueOf(200000.0), 12.0, false);

        when(service.getAllType()).thenReturn(Flux.just(testLoanType, secondLoanType));
        when(mapper.toDto(testLoanType)).thenReturn(testApplicationTypeDTO);
        when(mapper.toDto(secondLoanType)).thenReturn(secondApplicationTypeDTO);

        // When & Then
        StepVerifier.create(loanTypeController.getAll())
                .expectNext(testApplicationTypeDTO)
                .expectNext(secondApplicationTypeDTO)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería retornar lista vacía cuando no hay tipos de aplicación")
    void shouldReturnEmptyWhenNoApplicationTypes() {
        // Given
        when(service.getAllType()).thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(loanTypeController.getAll())
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería manejar error del caso de uso en creación")
    void shouldHandleUseCaseErrorOnCreate() {
        // Given
        RuntimeException expectedError = new RuntimeException("Database error");
        when(mapper.toEntity(testApplicationTypeDTO)).thenReturn(testLoanType);
        when(service.create(any(LoanType.class))).thenReturn(Mono.error(expectedError));

        // When & Then
        StepVerifier.create(loanTypeController.createApplicationType(testApplicationTypeDTO))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Debería manejar error del caso de uso en consulta")
    void shouldHandleUseCaseErrorOnGetAll() {
        // Given
        RuntimeException expectedError = new RuntimeException("Database connection failed");
        when(service.getAllType()).thenReturn(Flux.error(expectedError));

        // When & Then
        StepVerifier.create(loanTypeController.getAll())
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Debería manejar error del mapper en creación")
    void shouldHandleMapperErrorOnCreate() {
        // Given
        RuntimeException mapperError = new RuntimeException("Mapper error");
        when(mapper.toEntity(testApplicationTypeDTO)).thenThrow(mapperError);

        // When & Then
        StepVerifier.create(loanTypeController.createApplicationType(testApplicationTypeDTO))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Debería manejar error del mapper en consulta")
    void shouldHandleMapperErrorOnGetAll() {
        // Given
        RuntimeException mapperError = new RuntimeException("Mapping error");
        when(service.getAllType()).thenReturn(Flux.just(testLoanType));
        when(mapper.toDto(testLoanType)).thenThrow(mapperError);

        // When & Then
        StepVerifier.create(loanTypeController.getAll())
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Debería verificar el código de estado HTTP CREATED en creación exitosa")
    void shouldVerifyHttpStatusCreatedOnSuccessfulCreation() {
        // Given
        ApplicationTypeDTO createdApplicationTypeDTO = new ApplicationTypeDTO(3, testName,
                testMinimumAmount, testMaximumAmount, testInterestRate, testAutomaticValidation);
        LoanType createdLoanType = LoanType.builder()
                .id(3)
                .name(testName)
                .minimum_amount(testMinimumAmount)
                .maximum_amount(testMaximumAmount)
                .interest_rate(testInterestRate)
                .automatic_validation(testAutomaticValidation)
                .build();

        when(mapper.toEntity(testApplicationTypeDTO)).thenReturn(testLoanType);
        when(service.create(testLoanType)).thenReturn(Mono.just(createdLoanType));
        when(mapper.toDto(createdLoanType)).thenReturn(createdApplicationTypeDTO);

        // When & Then
        StepVerifier.create(loanTypeController.createApplicationType(testApplicationTypeDTO))
                .expectNextMatches(responseEntity ->
                        responseEntity.getStatusCode() == HttpStatus.CREATED &&
                                responseEntity.getBody().equals(createdApplicationTypeDTO)
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería manejar múltiples tipos de aplicación en getAll")
    void shouldHandleMultipleApplicationTypesInGetAll() {
        // Given
        LoanType type1 = LoanType.builder().id(1).name("Personal")
                .minimum_amount(BigDecimal.valueOf(1000.0)).maximum_amount(BigDecimal.valueOf(50000.0)).interest_rate(15.5).automatic_validation(true).build();
        LoanType type2 = LoanType.builder().id(2).name("Business")
                .minimum_amount(BigDecimal.valueOf(5000.0)).maximum_amount(BigDecimal.valueOf(200000.0)).interest_rate(12.0).automatic_validation(false).build();
        LoanType type3 = LoanType.builder().id(3).name("Mortgage")
                .minimum_amount(BigDecimal.valueOf(20000.0)).maximum_amount(BigDecimal.valueOf(500000.0)).interest_rate(8.5).automatic_validation(false).build();

        ApplicationTypeDTO dto1 = new ApplicationTypeDTO(1, "Personal", BigDecimal.valueOf(1000.0), BigDecimal.valueOf(50000.0), 15.5, true);
        ApplicationTypeDTO dto2 = new ApplicationTypeDTO(2, "Business", BigDecimal.valueOf(5000.0), BigDecimal.valueOf(200000.0), 12.0, false);
        ApplicationTypeDTO dto3 = new ApplicationTypeDTO(3, "Mortgage", BigDecimal.valueOf(20000.0), BigDecimal.valueOf(500000.0), 8.5, false);

        when(service.getAllType()).thenReturn(Flux.just(type1, type2, type3));
        when(mapper.toDto(type1)).thenReturn(dto1);
        when(mapper.toDto(type2)).thenReturn(dto2);
        when(mapper.toDto(type3)).thenReturn(dto3);

        // When & Then
        StepVerifier.create(loanTypeController.getAll())
                .expectNext(dto1)
                .expectNext(dto2)
                .expectNext(dto3)
                .verifyComplete();
    }
}