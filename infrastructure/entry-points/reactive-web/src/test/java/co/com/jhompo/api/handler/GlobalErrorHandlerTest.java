package co.com.jhompo.api.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.ConnectException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalErrorHandlerTest {

    private GlobalErrorHandler globalErrorHandler;

    @BeforeEach
    void setUp() {
        globalErrorHandler = new GlobalErrorHandler();
    }

    @Test
    void testHandleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid input");
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test").build()
        );

        Mono<ResponseEntity<ErrorResponse>> result = globalErrorHandler.handleIllegalArgument(ex, exchange);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(response.getBody().message()).isEqualTo("Invalid input");
                })
                .verifyComplete();
    }

    @Test
    void testHandleWebClientRequestExceptionWithConnectException() {
        WebClientRequestException ex = new WebClientRequestException(
                new ConnectException("Connection refused"),
                HttpMethod.GET,
                URI.create("http://localhost"),
                HttpHeaders.EMPTY
        );

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test").build()
        );

        Mono<ResponseEntity<ErrorResponse>> result = globalErrorHandler.handleWebClientRequestException(ex, exchange);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                    assertThat(response.getBody().error()).contains("Service Unavailable");
                })
                .verifyComplete();
    }

    @Test
    void testHandleWebClientRequestExceptionGeneric() {
        WebClientRequestException ex = new WebClientRequestException(
                new RuntimeException("Generic client error"),
                HttpMethod.POST,
                URI.create("http://localhost/api"),
                HttpHeaders.EMPTY
        );

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api").build()
        );

        Mono<ResponseEntity<ErrorResponse>> result = globalErrorHandler.handleWebClientRequestException(ex, exchange);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(response.getBody().error()).contains("Internal Server Error");
                })
                .verifyComplete();
    }

    @Test
    void testHandleDataIntegrityViolationDuplicateEmail() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("application_email_idx");
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/loan").build()
        );

        Mono<ResponseEntity<ErrorResponse>> result = globalErrorHandler.handleDataIntegrity(ex, exchange);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(response.getBody().error()).isEqualTo("Duplicate Key");
                })
                .verifyComplete();
    }

    @Test
    void testHandleGenericExceptionNullPointer() {
        Exception ex = new NullPointerException("Unexpected null");
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/null").build()
        );

        Mono<ResponseEntity<ErrorResponse>> result = globalErrorHandler.handleGenericException(ex, exchange);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(response.getBody().error()).isEqualTo("Null Pointer Exception");
                })
                .verifyComplete();
    }

    @Test
    void testHandleGenericExceptionIllegalState() {
        Exception ex = new IllegalStateException("Illegal state detected");
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/illegal").build()
        );

        Mono<ResponseEntity<ErrorResponse>> result = globalErrorHandler.handleGenericException(ex, exchange);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(response.getBody().error()).isEqualTo("Illegal State");
                })
                .verifyComplete();
    }
}
