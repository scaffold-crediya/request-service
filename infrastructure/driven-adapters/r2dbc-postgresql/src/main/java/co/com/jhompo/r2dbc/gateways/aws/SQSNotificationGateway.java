package co.com.jhompo.r2dbc.gateways.aws;

import co.com.jhompo.model.gateways.NotificationGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SQSNotificationGateway implements NotificationGateway {

    private final SqsAsyncClient sqsAsyncClient;
    private final String colaUrl = "https://sqs.us-east-1.amazonaws.com/771174006840/queue-approvals-requests";

    /*public SQSNotificationGateway(SqsAsyncClient sqsAsyncClient) {
        this.sqsAsyncClient = sqsAsyncClient;
    }*/

    public SQSNotificationGateway() {
        this.sqsAsyncClient = SqsAsyncClient.builder()
                .region(Region.of("us-east-1"))
                .build();
    }

    @Override
    public Mono<Void> sendNotification(String loanId, String status, String email) {
        String messageBody = String.format("{\"solicitudId\":\"%s\", \"estado\":\"%s\", \"email_destino\":\"%s\"}", loanId, status, email);

        log.info("Sending message to SQS: {}", messageBody);

        SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(colaUrl)
                .messageBody(messageBody)
                .build();


        return Mono.fromFuture(sqsAsyncClient.sendMessage(request))
                .doOnSuccess(response -> log.info("*****Message sent successfully. MessageId: {}", response.messageId()))
                .doOnError(error -> log.error("*****Failed to send message to SQS: {}", error.getMessage()))
                .then();


    }
}