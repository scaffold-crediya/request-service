package co.com.jhompo.api;

import co.com.jhompo.api.dtos.LoanApplicationDTO;
import co.com.jhompo.api.mapper.LoanApplicationMapper;
import co.com.jhompo.model.loanapplication.LoanApplication;
import co.com.jhompo.usecase.loanapplication.LoanApplicationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/solicitud")
public class LoanApplicationController {

    private final LoanApplicationUseCase loanApplicationUseCase;
    private final LoanApplicationMapper mapper;

    @PostMapping
    public Mono<LoanApplication> create(@RequestBody LoanApplicationDTO dto) {

        log.info("Recibiendo solicitud de creación: {}", dto);
        return loanApplicationUseCase.create(mapper.toEntityForCreate(dto))
                .doOnSuccess(app -> log.info("Solicitud creada: {}", app))
                .doOnError(error -> log.error("Error al crear solicitud", error));
    }

    @PutMapping("/{id}")
    public Mono<LoanApplication> update(@PathVariable UUID id, @RequestBody LoanApplication dto) {
        log.info(" Recibiendo solicitud de actualización ID={} con datos: {}", id, dto);
        dto.setId(id);
        return loanApplicationUseCase.update(dto)
                .doOnSuccess(app -> log.info("Solicitud actualizada: {}", app))
                .doOnError(error -> log.error("Error al actualizar solicitud ID={}", id, error));
    }

    @GetMapping
    public Flux<LoanApplication> getAll() {
        log.info("Listando todas las solicitudes");
        return loanApplicationUseCase.getAll()
                .doOnComplete(() -> log.info("Listado completo de solicitudes"))
                .doOnError(error -> log.error("Error al listar solicitudes", error));
    }

    @GetMapping("/{id}")
    public Mono<LoanApplication> getById(@PathVariable UUID id) {
        log.info("Consultando solicitud con ID={}", id);
        return loanApplicationUseCase.getById(id)
                .doOnSuccess(app -> log.info("Solicitud encontrada: {}", app))
                .doOnError(error -> log.error("Error al consultar solicitud ID={}", id, error));
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable UUID id) {
        log.info("Eliminando solicitud con ID={}", id);
        return loanApplicationUseCase.delete(id)
                .doOnSuccess(v -> log.info("Solicitud eliminada ID={}", id))
                .doOnError(error -> log.error("Error al eliminar solicitud ID={}", id, error));
    }
}