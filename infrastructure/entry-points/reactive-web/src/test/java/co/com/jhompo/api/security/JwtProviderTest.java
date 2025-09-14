package co.com.jhompo.api.security;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    private JwtProvider jwtProvider;
    private String secretKey;
    private String validToken;
    private String tokenWithoutRole;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();
        // Definimos una secretKey válida (>=32 bytes)
        secretKey = Base64.getEncoder().encodeToString("super-secret-key-for-jwt-testing-123456".getBytes());
        jwtProvider.secretKey = secretKey;

        SecretKey key = jwtProvider.getSigningKey();

        // Token con role
        validToken = Jwts.builder()
                .setSubject("test@example.com")
                .claim("role", "ADMIN")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 10000)) // 10 seg
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // Token sin role
        tokenWithoutRole = Jwts.builder()
                .setSubject("norole@example.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 10000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    @DisplayName("Debe validar un token válido")
    void shouldValidateValidToken() {
        assertTrue(jwtProvider.validateToken(validToken));
    }

    @Test
    @DisplayName("Debe rechazar un token inválido")
    void shouldRejectInvalidToken() {
        assertFalse(jwtProvider.validateToken("invalid-token"));
    }

    @Test
    @DisplayName("Debe obtener el email del token")
    void shouldGetEmailFromToken() {
        String email = jwtProvider.getEmailFromToken(validToken);
        assertEquals("test@example.com", email);
    }

    @Test
    @DisplayName("Debe obtener el rol del token")
    void shouldGetRolesFromToken() {
        List<String> roles = jwtProvider.getRolesFromToken(validToken);
        assertEquals(1, roles.size());
        assertEquals("ADMIN", roles.get(0));
    }

    @Test
    @DisplayName("Debe retornar lista vacía si el token no tiene rol")
    void shouldReturnEmptyListWhenNoRole() {
        List<String> roles = jwtProvider.getRolesFromToken(tokenWithoutRole);
        assertTrue(roles.isEmpty());
    }

    @Test
    @DisplayName("Debe generar la signingKey correctamente")
    void shouldGenerateSigningKey() {
        assertNotNull(jwtProvider.getSigningKey());
    }
}

