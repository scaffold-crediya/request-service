package co.com.jhompo.api;

import co.com.jhompo.api.dtos.ApplicationTypeDTO;
import co.com.jhompo.api.mapper.ApplicationTypeMapper;
import co.com.jhompo.model.applicationtype.ApplicationType;
import co.com.jhompo.usecase.applicationtype.ApplicationTypeUseCase;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationTypeControllerTest {

    @Mock
    private ApplicationTypeUseCase service;

    @Mock
    private ApplicationTypeMapper mapper;

    @InjectMocks
    private ApplicationTypeController applicationTypeController;

    private ApplicationTypeDTO testApplicationTypeDTO;
    private ApplicationType testApplicationType;
    private final int testId = 1;
    private final String testName = "Personal Loan";
    private final double testMinimumAmount = 1000.0;
    private final double testMaximumAmount = 50000.0;
    private final double testInterestRate = 15.5;
    private final boolean testAutomaticValidation = true;

    @BeforeEach
    void setUp() {
        testApplicationTypeDTO = new ApplicationTypeDTO(testId, testName, testMinimumAmount,
                testMaximumAmount, testInterestRate, testAutomaticValidation);

        testApplicationType = ApplicationType.builder()
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
        ApplicationType createdApplicationType = ApplicationType.builder()
                .id(5) // ID generado por DB
                .name(testName)
                .minimum_amount(testMinimumAmount)
                .maximum_amount(testMaximumAmount)
                .interest_rate(testInterestRate)
                .automatic_validation(testAutomaticValidation)
                .build();

        ApplicationTypeDTO createdApplicationTypeDTO = new ApplicationTypeDTO(5, testName,
                testMinimumAmount, testMaximumAmount, testInterestRate, testAutomaticValidation);

        when(mapper.toEntity(testApplicationTypeDTO)).thenReturn(testApplicationType);
        when(service.create(testApplicationType)).thenReturn(Mono.just(createdApplicationType));
        when(mapper.toDto(createdApplicationType)).thenReturn(createdApplicationTypeDTO);

        // When & Then
        StepVerifier.create(applicationTypeController.createApplicationType(testApplicationTypeDTO))
                .expectNext(new ResponseEntity<>(createdApplicationTypeDTO, HttpStatus.CREATED))
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería obtener todos los tipos de aplicación exitosamente")
    void shouldGetAllApplicationTypesSuccessfully() {
        // Given
        ApplicationType secondApplicationType = ApplicationType.builder()
                .id(2)
                .name("Business Loan")
                .minimum_amount(5000.0)
                .maximum_amount(200000.0)
                .interest_rate(12.0)
                .automatic_validation(false)
                .build();

        ApplicationTypeDTO secondApplicationTypeDTO = new ApplicationTypeDTO(2, "Business Loan",
                5000.0, 200000.0, 12.0, false);

        when(service.getAllStatuses()).thenReturn(Flux.just(testApplicationType, secondApplicationType));
        when(mapper.toDto(testApplicationType)).thenReturn(testApplicationTypeDTO);
        when(mapper.toDto(secondApplicationType)).thenReturn(secondApplicationTypeDTO);

        // When & Then
        StepVerifier.create(applicationTypeController.getAll())
                .expectNext(testApplicationTypeDTO)
                .expectNext(secondApplicationTypeDTO)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería retornar lista vacía cuando no hay tipos de aplicación")
    void shouldReturnEmptyWhenNoApplicationTypes() {
        // Given
        when(service.getAllStatuses()).thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(applicationTypeController.getAll())
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería manejar error del caso de uso en creación")
    void shouldHandleUseCaseErrorOnCreate() {
        // Given
        RuntimeException expectedError = new RuntimeException("Database error");
        when(mapper.toEntity(testApplicationTypeDTO)).thenReturn(testApplicationType);
        when(service.create(any(ApplicationType.class))).thenReturn(Mono.error(expectedError));

        // When & Then
        StepVerifier.create(applicationTypeController.createApplicationType(testApplicationTypeDTO))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Debería manejar error del caso de uso en consulta")
    void shouldHandleUseCaseErrorOnGetAll() {
        // Given
        RuntimeException expectedError = new RuntimeException("Database connection failed");
        when(service.getAllStatuses()).thenReturn(Flux.error(expectedError));

        // When & Then
        StepVerifier.create(applicationTypeController.getAll())
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
        StepVerifier.create(applicationTypeController.createApplicationType(testApplicationTypeDTO))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Debería manejar error del mapper en consulta")
    void shouldHandleMapperErrorOnGetAll() {
        // Given
        RuntimeException mapperError = new RuntimeException("Mapping error");
        when(service.getAllStatuses()).thenReturn(Flux.just(testApplicationType));
        when(mapper.toDto(testApplicationType)).thenThrow(mapperError);

        // When & Then
        StepVerifier.create(applicationTypeController.getAll())
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Debería verificar el código de estado HTTP CREATED en creación exitosa")
    void shouldVerifyHttpStatusCreatedOnSuccessfulCreation() {
        // Given
        ApplicationTypeDTO createdApplicationTypeDTO = new ApplicationTypeDTO(3, testName,
                testMinimumAmount, testMaximumAmount, testInterestRate, testAutomaticValidation);
        ApplicationType createdApplicationType = ApplicationType.builder()
                .id(3)
                .name(testName)
                .minimum_amount(testMinimumAmount)
                .maximum_amount(testMaximumAmount)
                .interest_rate(testInterestRate)
                .automatic_validation(testAutomaticValidation)
                .build();

        when(mapper.toEntity(testApplicationTypeDTO)).thenReturn(testApplicationType);
        when(service.create(testApplicationType)).thenReturn(Mono.just(createdApplicationType));
        when(mapper.toDto(createdApplicationType)).thenReturn(createdApplicationTypeDTO);

        // When & Then
        StepVerifier.create(applicationTypeController.createApplicationType(testApplicationTypeDTO))
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
        ApplicationType type1 = ApplicationType.builder().id(1).name("Personal")
                .minimum_amount(1000.0).maximum_amount(50000.0).interest_rate(15.5).automatic_validation(true).build();
        ApplicationType type2 = ApplicationType.builder().id(2).name("Business")
                .minimum_amount(5000.0).maximum_amount(200000.0).interest_rate(12.0).automatic_validation(false).build();
        ApplicationType type3 = ApplicationType.builder().id(3).name("Mortgage")
                .minimum_amount(20000.0).maximum_amount(500000.0).interest_rate(8.5).automatic_validation(false).build();

        ApplicationTypeDTO dto1 = new ApplicationTypeDTO(1, "Personal", 1000.0, 50000.0, 15.5, true);
        ApplicationTypeDTO dto2 = new ApplicationTypeDTO(2, "Business", 5000.0, 200000.0, 12.0, false);
        ApplicationTypeDTO dto3 = new ApplicationTypeDTO(3, "Mortgage", 20000.0, 500000.0, 8.5, false);

        when(service.getAllStatuses()).thenReturn(Flux.just(type1, type2, type3));
        when(mapper.toDto(type1)).thenReturn(dto1);
        when(mapper.toDto(type2)).thenReturn(dto2);
        when(mapper.toDto(type3)).thenReturn(dto3);

        // When & Then
        StepVerifier.create(applicationTypeController.getAll())
                .expectNext(dto1)
                .expectNext(dto2)
                .expectNext(dto3)
                .verifyComplete();
    }
}