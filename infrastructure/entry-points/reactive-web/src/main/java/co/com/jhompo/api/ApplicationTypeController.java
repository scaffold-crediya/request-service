package co.com.jhompo.api;

import co.com.jhompo.api.dtos.ApplicationTypeDTO;
import co.com.jhompo.api.dtos.status.StatusDTO;
import co.com.jhompo.api.mapper.ApplicationTypeMapper;
import co.com.jhompo.usecase.applicationtype.ApplicationTypeUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/loantype")
@RequiredArgsConstructor
public class ApplicationTypeController {

    private final ApplicationTypeUseCase service;
    private final ApplicationTypeMapper mapper;

    @PostMapping
    public Mono<ResponseEntity<ApplicationTypeDTO>> createApplicationType(@RequestBody ApplicationTypeDTO applicationTypeDTO) {
        return Mono.just(applicationTypeDTO)
                .map(mapper::toEntity)
                .flatMap(service::create)
                .map(mapper::toDto)
                .map(created -> new ResponseEntity<>(created, HttpStatus.CREATED));
    }

    @GetMapping
    public Flux<ApplicationTypeDTO> getAll() {
        return service.getAllStatuses()
                .map(mapper::toDto);
    }
}