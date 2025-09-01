package co.com.jhompo.r2dbc.application;

import co.com.jhompo.model.loanapplication.gateways.UserExistenceGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class EmailVerifyAdapter implements UserExistenceGateway {

    private final WebClient authWebClient; // Inyectaremos un WebClient configurado
    private final String uri = "/api/v1/usuarios/email/{email}";



    @Override
    public Mono<Boolean> userExistsByEmail(String email) {

        System.out.println("*****Email a validar: {}" + email);

        return authWebClient.get()
                .uri(uri, email)
                .retrieve()
                .bodyToMono(Map.class) // Usar Map genérico
                .doOnNext(userResponse -> {
                    System.out.println("Usuario encontrado: '{}'" + userResponse);
                })
                .map(userResponse -> {
                    if (userResponse == null || userResponse.isEmpty()) {
                        System.out.println("UserResponse es null o vacío");
                        return false;
                    }
                    boolean result = true; // Si hay Map con datos, el usuario existe
                    System.out.println("Usuario existe - Resultado: {}" + result);
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