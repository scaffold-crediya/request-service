package co.com.jhompo.r2dbc.gateways.aws;

import co.com.jhompo.model.loanapplication.dto.LoanValidation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SQSNotificationGatewayTest {

    @Mock
    private SqsAsyncClient sqsAsyncClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SQSNotificationGateway sqsNotificationGateway;

    private final String testValidationQueueUrl = "https://sqs.us-east-1.amazonaws.com/123456789/validation-queue";
    private final String testApprovalsQueueUrl = "https://sqs.us-east-1.amazonaws.com/123456789/approvals-queue";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sqsNotificationGateway, "validationQueueUrl", testValidationQueueUrl);
        ReflectionTestUtils.setField(sqsNotificationGateway, "approvalsQueueUrl", testApprovalsQueueUrl);
    }

    @Test
    @DisplayName("Debería enviar notificación exitosamente")
    void shouldSendNotificationSuccessfully() {
        // Given
        String loanId = "loan-123";
        String status = "APPROVED";
        String email = "test@example.com";

        SendMessageResponse mockResponse = SendMessageResponse.builder()
                .messageId("msg-123")
                .build();

        when(sqsAsyncClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // When & Then
        StepVerifier.create(sqsNotificationGateway.sendNotification(loanId, status, email))
                .verifyComplete();

        verify(sqsAsyncClient).sendMessage(argThat((SendMessageRequest request) ->
                request.queueUrl().equals(testApprovalsQueueUrl) &&
                        request.messageBody().contains(loanId) &&
                        request.messageBody().contains(status) &&
                        request.messageBody().contains(email)
        ));
    }

    @Test
    @DisplayName("Debería manejar error al enviar notificación")
    void shouldHandleNotificationSendError() {
        // Given
        String loanId = "loan-123";
        String status = "APPROVED";
        String email = "test@example.com";

        RuntimeException error = new RuntimeException("SQS connection failed");
        when(sqsAsyncClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.failedFuture(error));

        // When & Then
        StepVerifier.create(sqsNotificationGateway.sendNotification(loanId, status, email))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Debería enviar mensaje de validación exitosamente")
    void shouldSendValidationMessageSuccessfully() throws Exception {
        // Given
        LoanValidation loanValidation = new LoanValidation();
        loanValidation.setLoanId("loan-456");
        loanValidation.setUserName("Test User");
        loanValidation.setEmail("test@example.com");
        loanValidation.setAmount(10000.0);
        loanValidation.setTerm(12);
        loanValidation.setInterestRate(15.5);

        String jsonPayload = "{\"loanId\":\"loan-456\",\"userName\":\"Test User\"}";

        when(objectMapper.writeValueAsString(loanValidation)).thenReturn(jsonPayload);

        SendMessageResponse mockResponse = SendMessageResponse.builder()
                .messageId("validation-msg-123")
                .build();

        when(sqsAsyncClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // When & Then
        StepVerifier.create(sqsNotificationGateway.sendForValidation(loanValidation))
                .verifyComplete();

        verify(objectMapper).writeValueAsString(loanValidation);
        verify(sqsAsyncClient).sendMessage(argThat((SendMessageRequest request) ->
                request.queueUrl().equals(testValidationQueueUrl) &&
                        request.messageBody().equals(jsonPayload)
        ));
    }

    @Test
    @DisplayName("Debería manejar error de serialización JSON")
    void shouldHandleJsonSerializationError() throws Exception {
        // Given
        LoanValidation loanValidation = new LoanValidation();
        loanValidation.setLoanId("loan-456");

        when(objectMapper.writeValueAsString(loanValidation))
                .thenThrow(new RuntimeException("JSON serialization failed"));

        // When & Then
        StepVerifier.create(sqsNotificationGateway.sendForValidation(loanValidation))
                .expectError(RuntimeException.class)
                .verify();

        verify(sqsAsyncClient, never()).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    @DisplayName("Debería manejar error al enviar mensaje de validación")
    void shouldHandleValidationMessageSendError() throws Exception {
        // Given
        LoanValidation loanValidation = new LoanValidation();
        loanValidation.setLoanId("loan-456");

        String jsonPayload = "{\"loanId\":\"loan-456\"}";
        when(objectMapper.writeValueAsString(loanValidation)).thenReturn(jsonPayload);

        RuntimeException error = new RuntimeException("SQS send failed");
        when(sqsAsyncClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.failedFuture(error));

        // When & Then
        StepVerifier.create(sqsNotificationGateway.sendForValidation(loanValidation))
                .expectError(RuntimeException.class)
                .verify();
    }
}