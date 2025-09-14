package co.com.jhompo.api.security;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private WebFilterChain chain;

    @Mock
    private ServerHttpRequest request;

    @BeforeEach
    void setUp() {
        when(exchange.getRequest()).thenReturn(request);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("Debería continuar el filtro con un token válido")
    void shouldContinueFilterWithValidToken() {
        // Given
        String validToken = "Bearer valid_token";
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, validToken);

        // Mockea la llamada para que retorne el objeto HttpHeaders
        when(request.getHeaders()).thenReturn(headers);

        when(jwtProvider.validateToken(anyString())).thenReturn(true);
        when(jwtProvider.getEmailFromToken(anyString())).thenReturn("test@example.com");
        when(jwtProvider.getRolesFromToken(anyString())).thenReturn(List.of("USER"));

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        // Verifica que la cadena de filtros continúe
        verify(chain, times(1)).filter(exchange);
    }

    @Test
    @DisplayName("Debería continuar el filtro con un token inválido")
    void shouldContinueFilterWithInvalidToken() {
        // Given
        String invalidToken = "Bearer invalid_token";
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, invalidToken);

        // Mockea la llamada para que retorne el objeto HttpHeaders
        when(request.getHeaders()).thenReturn(headers);

        when(jwtProvider.validateToken(anyString())).thenReturn(false);

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        // Verifica que la cadena de filtros continúe
        verify(chain, times(1)).filter(exchange);
    }

    @Test
    @DisplayName("Debería continuar el filtro sin un token")
    void shouldContinueFilterWithoutToken() {
        // Given
        // Mockea la llamada a getHeaders para que retorne un objeto vacío
        when(request.getHeaders()).thenReturn(new HttpHeaders());

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        // Verifica que la cadena de filtros continúe
        verify(chain, times(1)).filter(exchange);
    }
}