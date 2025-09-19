package co.com.jhompo.sqs;

import co.com.jhompo.model.status.Status;
import co.com.jhompo.model.status.gateways.StatusRepository;
import co.com.jhompo.model.loanapplication.LoanApplication;
import co.com.jhompo.usecase.loanapplication.LoanApplicationUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LoanEventConsumerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private LoanApplicationUseCase loanUseCase;

    @Mock
    private StatusRepository statusRepository;

    private LoanEventConsumer loanEventConsumer;

    @BeforeEach
    void setUp() {
        loanEventConsumer = new LoanEventConsumer(objectMapper, loanUseCase, statusRepository);
    }

    @Test
    void shouldProcessValidMessageSuccessfully() throws JsonProcessingException {
        // Given
        String messageBody = """
            {
                "status": "APPROVED",
                "loanId": "123e4567-e89b-12d3-a456-426614174000"
            }
            """;

        Map<String, Object> parsedMessage = new HashMap<>();
        parsedMessage.put("status", "APPROVED");
        parsedMessage.put("loanId", "123e4567-e89b-12d3-a456-426614174000");

        UUID loanId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Status mockStatus = Status.builder().id(1).name("APPROVED").build();
        LoanApplication mockLoan = LoanApplication.builder().id(loanId).statusId(1).build();
        LoanApplication updatedLoan = LoanApplication.builder().id(loanId).statusId(1).build();

        when(objectMapper.readValue(messageBody, Map.class)).thenReturn(parsedMessage);
        when(statusRepository.findByName("APPROVED")).thenReturn(Mono.just(mockStatus));
        when(loanUseCase.getById(loanId)).thenReturn(Mono.just(mockLoan));
        when(loanUseCase.update(any(LoanApplication.class))).thenReturn(Mono.just(updatedLoan));

        // When & Then
        assertDoesNotThrow(() -> loanEventConsumer.processMessage(messageBody));

        verify(objectMapper, times(1)).readValue(messageBody, Map.class);
        verify(statusRepository, times(1)).findByName("APPROVED");
        verify(loanUseCase, times(1)).getById(loanId);
        verify(loanUseCase, times(1)).update(any(LoanApplication.class));
    }

    @Test
    void shouldHandleJsonProcessingException() throws JsonProcessingException {
        // Given
        String invalidMessageBody = "invalid-json";

        when(objectMapper.readValue(invalidMessageBody, Map.class))
                .thenThrow(new JsonProcessingException("Invalid JSON") {});

        // When & Then
        assertDoesNotThrow(() -> loanEventConsumer.processMessage(invalidMessageBody));

        verify(objectMapper, times(1)).readValue(invalidMessageBody, Map.class);
        verify(statusRepository, never()).findByName(anyString());
        verify(loanUseCase, never()).getById(any(UUID.class));
    }

    @Test
    void shouldHandleNullStatus() throws JsonProcessingException {
        // Given
        String messageBody = """
            {
                "status": null,
                "loanId": "123e4567-e89b-12d3-a456-426614174000"
            }
            """;

        Map<String, Object> parsedMessage = new HashMap<>();
        parsedMessage.put("status", null);
        parsedMessage.put("loanId", "123e4567-e89b-12d3-a456-426614174000");

        when(objectMapper.readValue(messageBody, Map.class)).thenReturn(parsedMessage);

        // When & Then
        assertDoesNotThrow(() -> loanEventConsumer.processMessage(messageBody));

        verify(objectMapper, times(1)).readValue(messageBody, Map.class);
        verify(statusRepository, never()).findByName(anyString());
        verify(loanUseCase, never()).getById(any(UUID.class));
    }

    @Test
    void shouldHandleBlankStatus() throws JsonProcessingException {
        // Given
        String messageBody = """
            {
                "status": "   ",
                "loanId": "123e4567-e89b-12d3-a456-426614174000"
            }
            """;

        Map<String, Object> parsedMessage = new HashMap<>();
        parsedMessage.put("status", "   ");
        parsedMessage.put("loanId", "123e4567-e89b-12d3-a456-426614174000");

        when(objectMapper.readValue(messageBody, Map.class)).thenReturn(parsedMessage);

        // When & Then
        assertDoesNotThrow(() -> loanEventConsumer.processMessage(messageBody));

        verify(objectMapper, times(1)).readValue(messageBody, Map.class);
        verify(statusRepository, never()).findByName(anyString());
        verify(loanUseCase, never()).getById(any(UUID.class));
    }

    @Test
    void shouldHandleInvalidUUID() throws JsonProcessingException {
        // Given
        String messageBody = """
            {
                "status": "APPROVED",
                "loanId": "invalid-uuid"
            }
            """;

        Map<String, Object> parsedMessage = new HashMap<>();
        parsedMessage.put("status", "APPROVED");
        parsedMessage.put("loanId", "invalid-uuid");

        when(objectMapper.readValue(messageBody, Map.class)).thenReturn(parsedMessage);

        // When & Then
        assertDoesNotThrow(() -> loanEventConsumer.processMessage(messageBody));

        verify(objectMapper, times(1)).readValue(messageBody, Map.class);
        verify(statusRepository, never()).findByName(anyString());
        verify(loanUseCase, never()).getById(any(UUID.class));
    }

    @Test
    void shouldHandleStatusNotFound() throws JsonProcessingException {
        // Given
        String messageBody = """
            {
                "status": "UNKNOWN_STATUS",
                "loanId": "123e4567-e89b-12d3-a456-426614174000"
            }
            """;

        Map<String, Object> parsedMessage = new HashMap<>();
        parsedMessage.put("status", "UNKNOWN_STATUS");
        parsedMessage.put("loanId", "123e4567-e89b-12d3-a456-426614174000");

        when(objectMapper.readValue(messageBody, Map.class)).thenReturn(parsedMessage);
        when(statusRepository.findByName("UNKNOWN_STATUS")).thenReturn(Mono.empty());

        // When & Then
        assertDoesNotThrow(() -> loanEventConsumer.processMessage(messageBody));

        verify(objectMapper, times(1)).readValue(messageBody, Map.class);
        verify(statusRepository, times(1)).findByName("UNKNOWN_STATUS");
        verify(loanUseCase, never()).getById(any(UUID.class));
    }

    @Test
    void shouldHandleLoanNotFound() throws JsonProcessingException {
        // Given
        String messageBody = """
            {
                "status": "APPROVED",
                "loanId": "123e4567-e89b-12d3-a456-426614174000"
            }
            """;

        Map<String, Object> parsedMessage = new HashMap<>();
        parsedMessage.put("status", "APPROVED");
        parsedMessage.put("loanId", "123e4567-e89b-12d3-a456-426614174000");

        UUID loanId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Status mockStatus = Status.builder().id(1).name("APPROVED").build();

        when(objectMapper.readValue(messageBody, Map.class)).thenReturn(parsedMessage);
        when(statusRepository.findByName("APPROVED")).thenReturn(Mono.just(mockStatus));
        when(loanUseCase.getById(loanId)).thenReturn(Mono.empty());

        // When & Then
        assertDoesNotThrow(() -> loanEventConsumer.processMessage(messageBody));

        verify(objectMapper, times(1)).readValue(messageBody, Map.class);
        verify(statusRepository, times(1)).findByName("APPROVED");
        verify(loanUseCase, times(1)).getById(loanId);
        verify(loanUseCase, never()).update(any(LoanApplication.class));
    }

    @Test
    void shouldHandleUpdateError() throws JsonProcessingException {
        // Given
        String messageBody = """
            {
                "status": "APPROVED",
                "loanId": "123e4567-e89b-12d3-a456-426614174000"
            }
            """;

        Map<String, Object> parsedMessage = new HashMap<>();
        parsedMessage.put("status", "APPROVED");
        parsedMessage.put("loanId", "123e4567-e89b-12d3-a456-426614174000");

        UUID loanId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Status mockStatus = Status.builder().id(1).name("APPROVED").build();
        LoanApplication mockLoan = LoanApplication.builder().id(loanId).statusId(1).build();

        when(objectMapper.readValue(messageBody, Map.class)).thenReturn(parsedMessage);
        when(statusRepository.findByName("APPROVED")).thenReturn(Mono.just(mockStatus));
        when(loanUseCase.getById(loanId)).thenReturn(Mono.just(mockLoan));
        when(loanUseCase.update(any(LoanApplication.class)))
                .thenReturn(Mono.error(new RuntimeException("Update failed")));

        // When & Then
        assertDoesNotThrow(() -> loanEventConsumer.processMessage(messageBody));

        verify(objectMapper, times(1)).readValue(messageBody, Map.class);
        verify(statusRepository, times(1)).findByName("APPROVED");
        verify(loanUseCase, times(1)).getById(loanId);
        verify(loanUseCase, times(1)).update(any(LoanApplication.class));
    }
}