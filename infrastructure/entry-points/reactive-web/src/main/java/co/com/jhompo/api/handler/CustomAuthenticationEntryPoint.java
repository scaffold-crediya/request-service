package co.com.jhompo.api.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import io.jsonwebtoken.security.SignatureException;

import java.time.Instant;

@Component
public class CustomAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException authException) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String message = "Token inválido o no proporcionado";

        Throwable cause = authException.getCause();
        if (cause instanceof ExpiredJwtException) {
            message = "Token expirado";
        } else if (cause instanceof SignatureException) {
            message = "Token con firma inválida";
        }

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now().toString(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                message,
                exchange.getRequest().getPath().toString()
        );

        try {
            byte[] jsonBytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = new DefaultDataBufferFactory().wrap(jsonBytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }
}
