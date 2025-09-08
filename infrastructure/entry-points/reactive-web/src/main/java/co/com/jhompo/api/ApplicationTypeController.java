package co.com.jhompo.api;

import co.com.jhompo.api.dtos.ApplicationTypeDTO;
import co.com.jhompo.api.mapper.ApplicationTypeMapper;
import co.com.jhompo.usecase.applicationtype.ApplicationTypeUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static co.com.jhompo.util.Messages.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/loantype")
@RequiredArgsConstructor
@Tag(name = APPLICATION_TYPE.TITLE, description = APPLICATION_TYPE.DESCRIPTION)
public class ApplicationTypeController {

    private final ApplicationTypeUseCase service;
    private final ApplicationTypeMapper mapper;

    @Operation(summary = APPLICATION_TYPE.DESCRIPTION_CREATE)
    @PostMapping
    public Mono<ResponseEntity<ApplicationTypeDTO>> createApplicationType(@RequestBody ApplicationTypeDTO applicationTypeDTO) {
        log.info(APPLICATION_TYPE.DESCRIPTION_CREATE);
        return Mono.just(applicationTypeDTO)
                .map(mapper::toEntity)
                .flatMap(service::create)
                .map(mapper::toDto)
                .map(created -> new ResponseEntity<>(created, HttpStatus.CREATED));
    }

    @Operation(summary = APPLICATION_TYPE.DESCRIPTION_GET_ALL)
    @GetMapping
    public Flux<ApplicationTypeDTO> getAll() {
        log.info(APPLICATION_TYPE.DESCRIPTION_GET_ALL);

        return service.getAllStatuses()
                .map(mapper::toDto);
    }
}