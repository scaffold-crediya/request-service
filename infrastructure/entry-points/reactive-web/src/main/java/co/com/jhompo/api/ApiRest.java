package co.com.jhompo.api;
import co.com.jhompo.api.dtos.LoanApplicationDTO;
import co.com.jhompo.api.mapper.LoanApplicationMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class ApiRest {
//    private final MyUseCase useCase;


    @GetMapping(path = "/usecase/path")
    public Mono<String> commandName() {
        return Mono.just("hola jhompo");
    }

    @PostMapping("/save")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> create(@RequestBody LoanApplicationDTO dto) {
         return Mono.just("Hola jhompo: " + dto.getEmail());
    }
}
