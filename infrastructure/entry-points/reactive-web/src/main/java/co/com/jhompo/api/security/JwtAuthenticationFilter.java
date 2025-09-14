package co.com.jhompo.api.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // Validar que el encabezado de autenticación exista y comience con "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // Validar el token y obtener los claims
            if (jwtProvider.validateToken(token)) {
                String email = jwtProvider.getEmailFromToken(token);

                // Obtener los roles del token. El método ya devuelve una lista.
                List<String> roles = jwtProvider.getRolesFromToken(token);
                log.info("************Roles obtenidos del token: {}", roles);

                // Mapear los roles a GrantedAuthority de Spring Security
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                log.info("*************Authorities finales: {}", authorities);

                // Crear el objeto de autenticación y ponerlo en el contexto de seguridad
                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(email, null, authorities);

                return chain.filter(exchange)
                        .contextWrite(ctx -> ctx.put("jwt", token))
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
            }
        }

        return chain.filter(exchange);
    }
}