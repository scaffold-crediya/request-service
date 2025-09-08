package co.com.jhompo.model.gateways;

import co.com.jhompo.model.loanapplication.dto.LoanValidation;
import reactor.core.publisher.Mono;

public interface NotificationGateway {
    Mono<Void> sendNotification(String loanId, String status, String email);

    // nuevo: enviar payload de validación a la cola (async)
    Mono<Void> sendForValidation(LoanValidation message);
}