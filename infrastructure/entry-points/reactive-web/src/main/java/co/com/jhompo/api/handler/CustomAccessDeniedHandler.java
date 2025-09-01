package co.com.jhompo.api.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class CustomAccessDeniedHandler implements ServerAccessDeniedHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, org.springframework.security.access.AccessDeniedException denied) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Crear el objeto ErrorResponse
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now().toString(),
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "Acceso denegado: no tienes permisos suficientes para acceder a este recurso.",
                exchange.getRequest().getPath().toString()
        );

        // Convertir el objeto a JSON y escribirlo en la respuesta
        try {
            byte[] jsonBytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = new DefaultDataBufferFactory().wrap(jsonBytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }
}

