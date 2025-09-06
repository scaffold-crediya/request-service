package co.com.jhompo.model.gateways;

import reactor.core.publisher.Mono;

public interface NotificationGateway {
    Mono<Void> sendNotification(String loanId, String status, String email);
}