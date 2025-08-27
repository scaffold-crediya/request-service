package co.com.jhompo.api;

import co.com.jhompo.api.dtos.LoanApplicationDTO;
import co.com.jhompo.model.loanapplication.LoanApplication;
import co.com.jhompo.usecase.loanapplication.LoanApplicationUseCase;
import mapper.LoanApplicationMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/solicitud")
public class LoanApplicationController {

    private final LoanApplicationUseCase loanApplicationUseCase;

    public LoanApplicationController(LoanApplicationUseCase loanApplicationUseCase) {
        this.loanApplicationUseCase = loanApplicationUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<LoanApplicationDTO> create(@RequestBody Mono<LoanApplicationDTO> loanApplicationDTO) {
        return loanApplicationDTO
                .map(LoanApplicationMapper::toDomain) // Convierte DTO a entidad de dominio
                .flatMap(loanApplicationUseCase::create)
                .map(LoanApplicationMapper::toDto); // DTO para la respuesta
    }

    @GetMapping("/{id}")
    public Mono<LoanApplicationDTO> findById(@PathVariable UUID id) {
        return loanApplicationUseCase.findById(id)
                .map(LoanApplicationMapper::toDto); // Convierte la entidad a DTO para la respuesta
    }

}
