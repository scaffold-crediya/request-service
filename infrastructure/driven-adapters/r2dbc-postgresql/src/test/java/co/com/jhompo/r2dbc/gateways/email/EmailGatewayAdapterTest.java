package co.com.jhompo.r2dbc.gateways.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailGatewayAdapterTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailGatewayAdapter emailGatewayAdapter;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        // Usa reflexión para establecer el campo 'fromEmail',
        // ya que @Value no funciona en tests unitarios.
        var field = EmailGatewayAdapter.class.getDeclaredField("fromEmail");
        field.setAccessible(true);
        field.set(emailGatewayAdapter, "test-from@example.com");
    }

    @Test
    void sendEmail_should_call_mailSender_with_correct_message() {
        // Arrange
        String toEmail = "test-to@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        // Act
        StepVerifier.create(emailGatewayAdapter.sendEmail(toEmail, subject, body))
                .verifyComplete();

        // Assert
        // Captura el objeto SimpleMailMessage que se le pasó al método mailSender.send()
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));

        // Otra forma de validar que los datos son correctos
        var captor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage sentMessage = captor.getValue();

        // Validamos el contenido del mensaje
        assert sentMessage.getFrom().equals("test-from@example.com");
        assert sentMessage.getTo()[0].equals(toEmail);
        assert sentMessage.getSubject().equals(subject);
        assert sentMessage.getText().equals(body);
    }
}