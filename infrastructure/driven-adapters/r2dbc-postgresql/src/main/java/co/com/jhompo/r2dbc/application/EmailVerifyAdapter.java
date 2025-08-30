package co.com.jhompo.r2dbc.application;

import co.com.jhompo.model.loanapplication.gateways.UserExistenceGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class EmailVerifyAdapter implements UserExistenceGateway {

    private final WebClient authWebClient; // Inyectaremos un WebClient configurado

    @Override
    public Mono<Boolean> userExistsByEmail(String email) {

        System.out.println("*****Email a validar: {}" + email);
        String uri = "/api/v1/usuarios/email/{email}";

        return authWebClient.get()
                .uri(uri, email)
                .retrieve()
                .toEntity(String.class) // Cambiar temporalmente para ver
                .doOnNext(response -> {
                    System.out.println("Status Code: {}" + response.getStatusCode());
                    System.out.println("Headers: {}" + response.getHeaders());
                    System.out.println("Body: '{}'" + response.getBody());
                })
                .map(response -> {
                    String body = response.getBody();
                    if (body == null) {
                        System.out.println("Body es null");
                        return false;
                    }
                    boolean result = Boolean.parseBoolean(body.trim());
                    System.out.println("Resultado: {}" +  result);
                    return result;
                })
                .doOnError(error -> {
                    System.out.println("Error en WebClient: " + error);
                })
                .onErrorResume(e -> {
                    System.out.println("Capturando error y retornando false: {}"+ e.getMessage());
                    return Mono.just(false);
                })
                .doFinally(signal -> {
                    System.out.println("=== FIN comprobarEmail - Signal: {} ===" + signal);
                });
    }

}