package co.com.jhompo.api;

import co.com.jhompo.api.dtos.ApplicationTypeDTO;
import co.com.jhompo.api.mapper.ApplicationTypeMapper;
import co.com.jhompo.usecase.applicationtype.ApplicationTypeUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/loantype")
@RequiredArgsConstructor
@Tag(name = "Tipos de Crédito", description = "Gestión de tipos de solicitud de crédito")
public class ApplicationTypeController {

    private final ApplicationTypeUseCase service;
    private final ApplicationTypeMapper mapper;

    @Operation(summary = "Crear nuevo tipo de crédito")
    @PostMapping
    public Mono<ResponseEntity<ApplicationTypeDTO>> createApplicationType(@RequestBody ApplicationTypeDTO applicationTypeDTO) {
        return Mono.just(applicationTypeDTO)
                .map(mapper::toEntity)
                .flatMap(service::create)
                .map(mapper::toDto)
                .map(created -> new ResponseEntity<>(created, HttpStatus.CREATED));
    }

    @Operation(summary = "Listar todos los tipos de crédito")
    @GetMapping
    public Flux<ApplicationTypeDTO> getAll() {
        return service.getAllStatuses()
                .map(mapper::toDto);
    }
}