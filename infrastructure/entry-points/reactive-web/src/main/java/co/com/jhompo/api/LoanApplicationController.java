package co.com.jhompo.api;

import co.com.jhompo.api.dtos.LoanApplicationDTO;
import co.com.jhompo.model.loanapplication.dto.LoanApplicationSummaryDTO;
import co.com.jhompo.api.mapper.LoanApplicationMapper;
import co.com.jhompo.model.loanapplication.LoanApplication;
import co.com.jhompo.usecase.loanapplication.LoanApplicationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/solicitud")
@Tag(name = "Solicitudes de Crédito", description = "Gestión de solicitudes de préstamo")
public class LoanApplicationController {

    private final LoanApplicationUseCase loanApplicationUseCase;
    private final LoanApplicationMapper mapper;

    @Operation(summary = "Crear nueva solicitud de crédito")
    @PostMapping("/registrar")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<LoanApplication> create(@RequestBody LoanApplicationDTO dto) {
        log.info("Recibiendo solicitud de creación: {}", dto);

        return getContext()
                .map(securityContext -> securityContext.getAuthentication())
                .map(Authentication::getPrincipal)
                .cast(String.class)
                .flatMap(authenticatedEmail -> {
                    if (!authenticatedEmail.equals(dto.getEmail())) {
                        return Mono.error(new IllegalArgumentException("No puede crear una solicitud a nombre de otro usuario."));
                    }
                    return loanApplicationUseCase.create(mapper.toEntityForCreate(dto));
                })
                .doOnSuccess(app -> log.info("*******Solicitud creada: {}", app))
                .doOnError(error -> log.error("********Error al crear solicitud", error));
    }

    @Operation(summary = "Actualizar solicitud existente")
    @PutMapping("/{id}")
    public Mono<LoanApplication> update(@PathVariable(value = "id") UUID id, @RequestBody LoanApplication dto) {
        log.info(" Recibiendo solicitud de actualización ID={} con datos: {}", id, dto);
        dto.setId(id);
        return loanApplicationUseCase.update(dto)
                .doOnSuccess(app -> log.info("Solicitud actualizada: {}", app))
                .doOnError(error -> log.error("Error al actualizar solicitud ID={}", id, error));
    }

    @Operation(summary = "Listar todas las solicitudes")
    @GetMapping
    public Flux<LoanApplication> getAll() {
        log.info("Listando todas las solicitudes");
        return loanApplicationUseCase.getAll()
                .doOnComplete(() -> log.info("Listado completo de solicitudes"))
                .doOnError(error -> log.error("Error al listar solicitudes", error));
    }

    @Operation(summary = "Buscar solicitud por ID")
    @GetMapping("/{id}")
    public Mono<LoanApplication> getById(@PathVariable(value = "id") UUID id) {
        log.info("Consultando solicitud con ID={}", id);
        return loanApplicationUseCase.getById(id)
                .doOnSuccess(app -> log.info("Solicitud encontrada: {}", app))
                .doOnError(error -> log.error("Error al consultar solicitud ID={}", id, error));
    }

    @Operation(summary = "Eliminar solicitud")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable(value = "id") UUID id) {
        log.info("Eliminando solicitud con ID={}", id);
        return loanApplicationUseCase.delete(id)
                .doOnSuccess(v -> log.info("Solicitud eliminada ID={}", id))
                .doOnError(error -> log.error("Error al eliminar solicitud ID={}", id, error));
    }


    @Operation(summary = "Listar solicitudes por estado paginado y filtrado")
    @GetMapping("/estado/{statusName}")
    public Flux<LoanApplicationSummaryDTO> getSummariesByStatus( @PathVariable("statusName") String statusName,
                                                                 @RequestParam(name = "page", defaultValue = "0") int page,
                                                                 @RequestParam(name = "size", defaultValue = "10") int size) {

        return loanApplicationUseCase.findByStatusName(statusName, page, size);
    }
}