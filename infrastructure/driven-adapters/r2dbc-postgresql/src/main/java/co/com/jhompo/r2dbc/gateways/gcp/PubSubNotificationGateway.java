package co.com.jhompo.r2dbc.gateways.gcp;

import co.com.jhompo.model.gateways.NotificationGateway;
import co.com.jhompo.model.loanapplication.dto.LoanValidation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;


@Slf4j
@Repository
@Primary
public class PubSubNotificationGateway implements NotificationGateway {

    private final ObjectMapper objectMapper;
    private final Publisher publisher;

    //Usar @Value para obtener las propiedades de application.yml
    @Value("${gcp.project-id}")
    private String projectId;

    @Value("${gcp.pubsub.validation-topic-id}")
    private String validationTopicId;

    public PubSubNotificationGateway(
            ObjectMapper objectMapper,
            @Value("${gcp.project-id}") String projectId,
            @Value("${gcp.pubsub.validation-topic-id}") String validationTopicId
    ) throws IOException {
        this.objectMapper = objectMapper;
        this.projectId = projectId;
        this.validationTopicId = validationTopicId;

        TopicName topicName = TopicName.of(projectId, validationTopicId);
        this.publisher = Publisher.newBuilder(topicName).build();
    }

    @PreDestroy
    public void shutdown() throws Exception {
        if (publisher != null) {
            publisher.shutdown();
            publisher.awaitTermination(1, TimeUnit.MINUTES);
        }
    }

    @Override
    public Mono<Void> sendNotification(String loanId, String status, String email) {
        // En esta implementación, solo nos enfocamos en el metodo 'sendForValidation'
        // que es el que activa el flujo de validación.
        return Mono.error(new UnsupportedOperationException("This method is not implemented for Pub/Sub gateway."));
    }

    @Override
    public Mono<Void> sendForValidation(LoanValidation message) {
        return Mono.create(sink -> {
            try {
                String payload = objectMapper.writeValueAsString(message);
                ByteString data = ByteString.copyFromUtf8(payload);
                PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
                ApiFuture<String> future = publisher.publish(pubsubMessage);
                ApiFutures.addCallback(future, new ApiFutureCallback<String>() {
                    @Override
                    public void onSuccess(String messageId) {
                        log.info("Message published to Pub/Sub with ID: {}", messageId);
                        sink.success();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        log.error("Error publishing message to Pub/Sub: {}", throwable.getMessage());
                        sink.error(throwable);
                    }
                }, directExecutor());
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }
}
