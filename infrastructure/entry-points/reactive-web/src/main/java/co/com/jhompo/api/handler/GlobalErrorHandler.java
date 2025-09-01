package co.com.jhompo.api.handler;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ControllerAdvice
public class GlobalErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 1) Errores de validación de argumentos
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleIllegalArgument(
            IllegalArgumentException ex, ServerWebExchange exchange) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid argument",
                ex.getMessage(),
                exchange.getRequest().getURI().getPath()
        );
    }

    // 2) Errores de seguridad (Spring Security)
   /* @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleAccessDenied(
            AccessDeniedException ex, ServerWebExchange exchange) {
        log.warn("Access denied: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "Access denied",
                ex.getMessage(),
                exchange.getRequest().getURI().getPath()
        );
    } */

    @ExceptionHandler(WebClientRequestException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleWebClientRequestException(WebClientRequestException ex, ServerWebExchange exchange) {
        // Check if the underlying cause is a connection failure
        if (ex.getCause() instanceof ConnectException) {
            return buildErrorResponse(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                    "El servicio no está disponible. Intente de nuevo más tarde.",
                    exchange.getRequest().getPath().toString()
            );
        }

        // Handle other types of WebClientRequestException
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Ocurrió un error al comunicarse con un servicio externo.",
                exchange.getRequest().getPath().toString()
        );
    }


    // 3) Handler genérico para DataIntegrityViolationException
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDataIntegrity(
            DataIntegrityViolationException ex, ServerWebExchange exchange) {

        String errorMessage = ex.getMessage();
        log.error("Database error: {}", errorMessage);

        // Verificar si es el error específico del índice
        if (errorMessage != null && errorMessage.contains("application_email_idx")) {
            return buildErrorResponse(
                    HttpStatus.CONFLICT,
                    "Duplicate Key",
                    "Ya existe una solicitud con este email para el tipo de préstamo seleccionado.",
                    exchange.getRequest().getURI().getPath()
            );
        }

        // Manejo genérico
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Data integrity violation",
                "El registro ya existe o viola una restricción de la base de datos.",
                exchange.getRequest().getURI().getPath()
        );
    }

    // 4) Catch-all para cualquier otra excepción
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(
            Exception ex, ServerWebExchange exchange) {
        log.error("Unhandled exception", ex);

        if (ex instanceof NullPointerException) {
            return buildErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Null Pointer Exception",
                    "Se encontró un valor nulo inesperado.",
                    exchange.getRequest().getURI().getPath()
            );
        }

        if (ex instanceof IllegalStateException) {
            return buildErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    "Illegal State",
                    ex.getMessage(),
                    exchange.getRequest().getURI().getPath()
            );
        }

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                ex.getMessage(),
                exchange.getRequest().getURI().getPath()
        );
    }

    // Metodo auxiliar
    private Mono<ResponseEntity<ErrorResponse>> buildErrorResponse(
            HttpStatus status, String error, String message, String path) {

        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now().format(FORMATTER),
                status.value(),
                error,
                message,
                path
        );

        return Mono.just(ResponseEntity.status(status).body(body));
    }
}
