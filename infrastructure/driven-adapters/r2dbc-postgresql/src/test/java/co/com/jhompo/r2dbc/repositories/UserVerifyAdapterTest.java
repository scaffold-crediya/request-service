package co.com.jhompo.r2dbc.repositories;
import co.com.jhompo.model.user.User;
import co.com.jhompo.r2dbc.repositories.loan_application.UserVerifyAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import java.util.List;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class UserVerifyAdapterTest {

    private WebClient mockWebClient;
    private WebClient.RequestHeadersUriSpec mockRequestHeadersUriSpec;
    private WebClient.RequestHeadersSpec mockRequestHeadersSpec;
    private WebClient.ResponseSpec mockResponseSpec;
    private WebClient.RequestBodyUriSpec mockRequestBodyUriSpec;
    private WebClient.RequestBodySpec mockRequestBodySpec;

    private UserVerifyAdapter userVerifyAdapter;

    @BeforeEach
    void setUp() {
        mockWebClient = mock(WebClient.class);
        userVerifyAdapter = new UserVerifyAdapter(mockWebClient);

        // Configuración común para las pruebas
        mockRequestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        mockRequestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        mockResponseSpec = mock(WebClient.ResponseSpec.class);
        mockRequestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        mockRequestBodySpec = mock(WebClient.RequestBodySpec.class);

        // Usa lenient() para permitir que estos stubs no se utilicen en todos los tests
        lenient().when(mockWebClient.get()).thenReturn(mockRequestHeadersUriSpec);
        lenient().when(mockWebClient.post()).thenReturn(mockRequestBodyUriSpec);
    }

    @Test
    @DisplayName("should return true when user exists (2xx status code)")
    void userExistsByEmail_should_return_true_on_success() {
        // Arrange
        String email = "test@example.com";
        when(mockRequestHeadersUriSpec.uri(anyString(), eq(email))).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.toBodilessEntity()).thenReturn(Mono.just(new ResponseEntity<>(HttpStatus.OK)));

        // Act & Assert
        StepVerifier.create(userVerifyAdapter.userExistsByEmail(email))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("should return false when user does not exist (non-2xx status code)")
    void userExistsByEmail_should_return_false_on_non_2xx() {
        // Arrange
        String email = "test@example.com";
        when(mockRequestHeadersUriSpec.uri(anyString(), eq(email))).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.toBodilessEntity()).thenReturn(Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));

        // Act & Assert
        StepVerifier.create(userVerifyAdapter.userExistsByEmail(email))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("should return false when the call fails")
    void userExistsByEmail_should_return_false_on_error() {
        // Arrange
        String email = "test@example.com";
        when(mockRequestHeadersUriSpec.uri(anyString(), eq(email))).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.toBodilessEntity()).thenReturn(Mono.error(new RuntimeException("Connection error")));

        // Act & Assert
        StepVerifier.create(userVerifyAdapter.userExistsByEmail(email))
                .expectNext(false)
                .verifyComplete();
    }



    @Test
    @DisplayName("should find user details by emails and add Authorization header from context")
    void findUserDetailsByEmails_should_return_user_details() {
        // Arrange
        List<String> emails = List.of("test1@example.com", "test2@example.com");
        String jwtToken = "mock_jwt_token";
        User user1 = User.builder().email("test1@example.com").build();
        User user2 = User.builder().email("test2@example.com").build();

        when(mockRequestBodyUriSpec.uri(anyString())).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.headers(any())).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.bodyValue(any())).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToFlux(User.class)).thenReturn(Flux.just(user1, user2));

        // Act & Assert
        StepVerifier.create(userVerifyAdapter.findUserDetailsByEmails(emails)
                        .contextWrite(Context.of("jwt", jwtToken)))
                .expectNext(user1, user2)
                .verifyComplete();

        // Verificar que el header fue establecido
        ArgumentCaptor<Consumer<HttpHeaders>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(mockRequestBodySpec).headers(captor.capture());

        HttpHeaders headers = new HttpHeaders();
        captor.getValue().accept(headers);

        assertEquals("Bearer " + jwtToken, headers.getFirst(HttpHeaders.AUTHORIZATION));
    }



    @Test
    @DisplayName("should propagate error when find user details call fails")
    void findUserDetailsByEmails_should_propagate_error() {
        // Arrange
        List<String> emails = List.of("test1@example.com");
        String jwtToken = "mock_jwt_token";

        when(mockRequestBodyUriSpec.uri(anyString())).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.headers(any())).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.bodyValue(any())).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToFlux(User.class)).thenReturn(Flux.error(new RuntimeException("API error")));

        // Act & Assert
        StepVerifier.create(userVerifyAdapter.findUserDetailsByEmails(emails)
                        .contextWrite(Context.of("jwt", jwtToken)))
                .expectErrorMatches(e -> e.getMessage().equals("API error"))
                .verify();

        // Verificar que el header fue establecido
        ArgumentCaptor<Consumer<HttpHeaders>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(mockRequestBodySpec).headers(captor.capture());

        HttpHeaders headers = new HttpHeaders();
        captor.getValue().accept(headers);

        assertEquals("Bearer " + jwtToken, headers.getFirst(HttpHeaders.AUTHORIZATION));
    }
}