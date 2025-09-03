package co.com.jhompo.r2dbc.email;

import co.com.jhompo.model.gateways.EmailGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Repository
@RequiredArgsConstructor
public class EmailGatewayAdapter implements EmailGateway {
    @Value("${spring.mail.username}")
    private String fromEmail;

    private final JavaMailSender mailSender;


    @Override
    public Mono<Void> sendEmail(String toEmail, String subject, String body) {
        return Mono.fromRunnable(() -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}