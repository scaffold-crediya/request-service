package co.com.jhompo.r2dbc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${microservices.autenticacion.url}")
    private String microserAuthenticacionUrl;

    @Bean
    public WebClient autenticacionBaseWebClient() {
        return WebClient.builder()
                .baseUrl(microserAuthenticacionUrl)
                .build();
    }
}
