package co.com.jhompo.api.handler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.access.AccessDeniedException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class CustomAccessDeniedHandlerTest {

    private final CustomAccessDeniedHandler handler = new CustomAccessDeniedHandler();

    @Test
    @DisplayName("Deberia devolver 403 con mensaje de acceso denegado")
    void shouldReturnForbiddenErrorResponse() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/secure-endpoint").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        MockServerHttpResponse response = exchange.getResponse();

        // Act
        Mono<Void> result = handler.handle(exchange, new AccessDeniedException("Denied"));

        // Assert
        StepVerifier.create(result).verifyComplete();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = response.getBodyAsString().block();
        assertThat(body).contains("Acceso denegado");
        assertThat(body).contains("/secure-endpoint");
    }
}
