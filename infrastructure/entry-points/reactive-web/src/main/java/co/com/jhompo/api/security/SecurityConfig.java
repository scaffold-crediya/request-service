package co.com.jhompo.api.security;

import co.com.jhompo.api.handler.CustomAccessDeniedHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;

import static co.com.jhompo.util.Messages.ROLE.*;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

        return http
                // Deshabilitar CSRF para API REST
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.disable()) // Deshabilitar CORS para usar el filtro personalizado si es necesario
                .authorizeExchange(exchanges -> exchanges
                        // Permitir acceso sin autenticación a la documentación de la API
                        .pathMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/webjars/**"
                        ).permitAll()
                        .pathMatchers("/api/v1/solicitud").hasAnyAuthority(ADMIN, ASESOR) // POST
                        .pathMatchers("/api/v1/solicitud/registrar").hasAnyAuthority( CLIENTE) // POST
                        .pathMatchers("/api/v1/solicitud/{id}").hasAnyAuthority(ADMIN, ASESOR, CLIENTE) // GET, PUT, DELETE
                        .pathMatchers("/api/v1/solicitud/estado/{name}",
                                                 "/api/v1/solicitud/{id}/status/{statusId}").hasAnyAuthority(ADMIN, ASESOR) // GET
                        .pathMatchers("/api/v1/loantype").hasAuthority(ADMIN) // POST, GET
                        .pathMatchers("/api/v1/loantype/{id}").hasAuthority(ADMIN) // PUT, GET, DELETE
                        .pathMatchers("/api/v1/statuses").hasAuthority(ADMIN) // POST, GET
                        .pathMatchers("/api/v1/statuses/{id}").hasAuthority(ADMIN) // PUT, GET, DELETE
                        // Todas las demás peticiones deben estar autenticadas
                        .anyExchange().authenticated()
                )
                .exceptionHandling(e -> e.accessDeniedHandler(new CustomAccessDeniedHandler()))
                // Agregar nuestro filtro JWT a la cadena de filtros de seguridad
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable) // Deshabilitar la autenticación HTTP básica y el form inicio de sesión
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .build();
    }

}
