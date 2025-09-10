package co.com.jhompo.sqs;

import co.com.jhompo.model.status.gateways.StatusRepository;
import co.com.jhompo.usecase.loanapplication.LoanApplicationUseCase;
import co.com.jhompo.util.Messages.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoanEventConsumer {

    private final ObjectMapper objectMapper;
    private final LoanApplicationUseCase loanUseCase;
    private final StatusRepository statusRepository;

    @SqsListener("queue-requests")
    public void processMessage(String messageBody) {
        log.info(SYSTEM.SQS_PROCESS, messageBody);

        try {
            Map<String, Object> parsedMessage = objectMapper.readValue(messageBody, Map.class);

            String status = (String) parsedMessage.get("status");
            String loanIdStr = (String) parsedMessage.get("loanId");
            UUID loanId = UUID.fromString(loanIdStr);

            log.info("Status: {}, LoanId: {}", status, loanId);

            if (status != null && !status.isBlank()) {
                statusRepository.findByName(status)
                        .flatMap(objStatus -> loanUseCase.getById(loanId)
                                .flatMap(loan -> {
                                    loan.setStatusId(objStatus.getId());
                                    return loanUseCase.update(loan);
                                }))
                        .doOnSuccess(updated -> log.info(SYSTEM.OPERATION_SUCCESS, status))
                        .doOnError(error -> log.error(SYSTEM.OPERATION_SUCCESS, error.getMessage()))
                        .subscribe();
            }

        } catch (JsonProcessingException e) {
            log.error(SYSTEM.SERIALIZATION_ERROR, e.getMessage());
            log.error(SYSTEM.CONTENT_ERROR, messageBody);
        } catch (Exception e) {
            log.error(SYSTEM.SQS_ERROR, e.getMessage(), e);
        }
    }
}
