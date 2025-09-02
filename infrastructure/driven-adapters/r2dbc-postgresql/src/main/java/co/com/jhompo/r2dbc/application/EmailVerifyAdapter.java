package co.com.jhompo.r2dbc.application;

import co.com.jhompo.common.Messages.*;
import co.com.jhompo.model.user.User;
import co.com.jhompo.model.user.gateways.UserExistenceGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EmailVerifyAdapter implements UserExistenceGateway {

    private final WebClient authWebClient; // Inyectaremos un WebClient configurado
    private final String uri = "/api/v1/usuarios";

    // El metodo original se queda como estaba (con el código limpio)
    @Override
    public Mono<Boolean> userExistsByEmail(String email) {
        String parameter = "/email/{email}";
        return authWebClient.get()
                .uri(uri+parameter,email)
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .onErrorReturn(false);
    }


    @Override
    public Flux<User> findUserDetailsByEmails(List<String> emails) {
        String parameter = "/details-by-email";
        return Flux.deferContextual(ctx -> {
            String token = ctx.get("jwt"); // Recuperamos el token del Reactor Context

            return authWebClient.post()
                    .uri(uri + parameter)
                    .headers(headers -> headers.setBearerAuth(token))
                    .bodyValue(emails)
                    .retrieve()
                    .bodyToFlux(User.class);
        });
    }

}