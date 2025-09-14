package co.com.jhompo.config;

import co.com.jhompo.model.gateways.EmailGateway;
import co.com.jhompo.model.loanapplication.gateways.LoanApplicationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(UseCasesConfig.class)
public class UseCasesConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @MockBean
    private EmailGateway emailGateway;

    // ... y si tu UseCasesConfig usa otros gateways, también debes mockearlos.
    @MockBean
    private LoanApplicationRepository loanApplicationRepository;

    @Test
    void testUseCaseBeansExist() {
        // Usa el contexto inyectado por Spring Boot
        String[] beanNames = applicationContext.getBeanDefinitionNames();

        boolean useCaseBeanFound = false;
        for (String beanName : beanNames) {
            if (beanName.endsWith("UseCase")) {
                useCaseBeanFound = true;
                break;
            }
        }

        assertTrue(useCaseBeanFound, "No beans ending with 'UseCase' were found");
    }


    static class MyUseCase {
        public String execute() {
            return "MyUseCase Test";
        }
    }
}