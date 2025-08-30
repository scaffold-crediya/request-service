package co.com.jhompo.api.handler;

public record ErrorResponse(
        String timestamp,
        int status,
        String error,
        String message,
        String path
) {}
