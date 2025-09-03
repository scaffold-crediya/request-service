package co.com.jhompo.usecase.email;

import co.com.jhompo.model.gateways.EmailGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class EmailUseCase {

    private final EmailGateway emailGateway;

    public Mono<Void> sendEmail(String to, String subject, String body) {
        return emailGateway.sendEmail(to,  subject, body);
    }

}
