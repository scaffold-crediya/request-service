package co.com.jhompo.api.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.AuthenticationException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class CustomAuthenticationEntryPointTest {

    private CustomAuthenticationEntryPoint entryPoint;

    @BeforeEach
    void setUp() {
        entryPoint = new CustomAuthenticationEntryPoint();
    }

    @Test
    void shouldReturnUnauthorizedForGenericAuthenticationException() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test").build()
        );

        AuthenticationException exception = Mockito.mock(AuthenticationException.class);
        Mockito.when(exception.getCause()).thenReturn(null);

        Mono<Void> result = entryPoint.commence(exchange, exception);

        StepVerifier.create(result).verifyComplete();

        ServerHttpResponse response = exchange.getResponse();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().getContentType().toString())
                .isEqualTo("application/json");
    }

    @Test
    void shouldReturnMessageForExpiredJwtException() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/expired").build()
        );

        AuthenticationException exception = Mockito.mock(AuthenticationException.class);
        Mockito.when(exception.getCause()).thenReturn(new ExpiredJwtException(null, null, "Expired"));

        Mono<Void> result = entryPoint.commence(exchange, exception);

        StepVerifier.create(result).verifyComplete();

        byte[] responseBytes = exchange.getResponse().getBody().blockFirst().asByteBuffer().array();
        String responseBody = new String(responseBytes);

        assertThat(responseBody).contains("Token expirado");
    }

   /* @Test
    void shouldReturnMessageForSignatureException() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/signature").build()
        );

        AuthenticationException exception = Mockito.mock(AuthenticationException.class);
        Mockito.when(exception.getCause()).thenReturn(new SignatureException("Invalid signature"));

        Mono<Void> result = entryPoint.commence(exchange, exception);

        StepVerifier.create(result).verifyComplete();

        byte[] responseBytes = exchange.getResponse().getBody().blockFirst().asByteBuffer().array();
        String responseBody = new String(responseBytes);

        assertThat(responseBody).contains("Token con firma inválida");
    }*/
}
