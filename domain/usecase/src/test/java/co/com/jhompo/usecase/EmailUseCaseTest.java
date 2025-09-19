package co.com.jhompo.usecase;

import co.com.jhompo.model.gateways.EmailGateway;
import co.com.jhompo.usecase.email.EmailUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailUseCaseTest {

    @Mock
    private EmailGateway emailGateway;

    @InjectMocks
    private EmailUseCase emailUseCase;

    private final String to = "test@example.com";
    private final String subject = "Test Subject";
    private final String body = "Test Body";

    @BeforeEach
    void setUp() {
        // Mockear el comportamiento del gateway
        when(emailGateway.sendEmail(to, subject, body)).thenReturn(Mono.empty());
    }

    @Test
    void sendEmail_should_call_gateway_with_correct_parameters() {
        // Act
        Mono<Void> result = emailUseCase.sendEmail(to, subject, body);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        // Verificar que el método sendEmail del gateway fue llamado
        verify(emailGateway).sendEmail(to, subject, body);
    }
}
