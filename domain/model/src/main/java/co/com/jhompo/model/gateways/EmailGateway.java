package co.com.jhompo.model.gateways;

import reactor.core.publisher.Mono;

public interface EmailGateway {
    Mono<Void> sendEmail(String to, String subject, String body);
}
