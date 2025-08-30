package co.com.jhompo.model.loanapplication.gateways;

import reactor.core.publisher.Mono;

public interface UserExistenceGateway {

    Mono<Boolean> userExistsByEmail(String email);
}
