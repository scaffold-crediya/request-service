package co.com.jhompo.r2dbc.gateways.aws;

import co.com.jhompo.model.gateways.NotificationGateway;
import co.com.jhompo.model.loanapplication.dto.LoanValidation;
import co.com.jhompo.util.Messages.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SQSNotificationGateway implements NotificationGateway {

    private final SqsAsyncClient sqsAsyncClient;
    private final ObjectMapper objectMapper;


    @Value("${aws.sqs.validation-queue-url}")
    private String validationQueueUrl;

    @Value("${aws.sqs.approvals-queue-url}")
    private String approvalsQueueUrl;


    @Override
    public Mono<Void> sendNotification(String loanId, String status, String email) {
        String messageBody = String.format("{\"solicitudId\":\"%s\", \"estado\":\"%s\", \"email_destino\":\"%s\"}", loanId, status, email);

        log.info(SYSTEM.SQS_PROCESS, messageBody);

        SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(approvalsQueueUrl)
                .messageBody(messageBody)
                .build();


        return Mono.fromFuture(sqsAsyncClient.sendMessage(request))
                .doOnSuccess(response -> log.info(SYSTEM.OPERATION_SUCCESS, response.messageId()))
                .doOnError(error -> log.error(SYSTEM.SQS_ERROR, error.getMessage()))
                .then();
    }


    @Override
    public Mono<Void> sendForValidation(LoanValidation message) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            SendMessageRequest req = SendMessageRequest.builder()
                    .queueUrl(validationQueueUrl)
                    .messageBody(payload)
                    .build();

            return Mono.fromFuture(sqsAsyncClient.sendMessage(req)).then();
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}