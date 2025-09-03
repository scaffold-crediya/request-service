package co.com.jhompo.model.user.gateways;


import co.com.jhompo.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface UserExistenceGateway {

    Mono<Boolean> userExistsByEmail(String email);

    Flux<User> findUserDetailsByEmails(List<String> emails);

}
