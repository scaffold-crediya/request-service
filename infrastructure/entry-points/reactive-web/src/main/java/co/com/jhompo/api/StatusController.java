package co.com.jhompo.api;


import co.com.jhompo.api.dtos.status.StatusDTO;
import co.com.jhompo.api.dtos.status.StatusRequestDTO;
import co.com.jhompo.api.mapper.StatusMapper;
import co.com.jhompo.usecase.status.StatusUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static co.com.jhompo.common.Messages.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/statuses")
@RequiredArgsConstructor
@Tag(name = STATUS.TITLE, description = STATUS.DESCRIPTION)
public class StatusController {

    private final StatusUseCase statusUseCase;
    private final StatusMapper statusMapper;


    @Operation(summary = STATUS.DESCRIPTION_GET_ALL)
    @GetMapping
    public Flux<StatusDTO> getAll() {
        log.info(STATUS.DESCRIPTION_GET_ALL);
        return statusUseCase.getAllStatuses()
                .map(statusMapper::toDto);
    }

    @Operation(summary = STATUS.DESCRIPTION_FIND_BY_ID)
    @GetMapping("/{id}")
    public Mono<StatusDTO> getById(@PathVariable Integer id) {
        log.info(STATUS.DESCRIPTION_FIND_BY_ID);
        return statusUseCase.getStatusById(id)
                .map(statusMapper::toDto);
    }


    @Operation(summary = STATUS.DESCRIPTION_CREATE)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<StatusDTO> create(@RequestBody StatusRequestDTO requestDTO) {
        log.info(STATUS.DESCRIPTION_CREATE);
        var status = statusMapper.toDomain(0, requestDTO);  // El ID '0' es un placeholder, ya que la base de datos lo generará.
        return statusUseCase.createStatus(status)
                .map(statusMapper::toDto);
    }


    @Operation(summary = STATUS.DESCRIPTION_UPDATE)
    @PutMapping("/{id}")
    public Mono<StatusDTO> update(@PathVariable Integer id, @RequestBody StatusRequestDTO requestDTO) {
        log.info(STATUS.DESCRIPTION_UPDATE);
        var status = statusMapper.toDomain(id, requestDTO);
        return statusUseCase.updateStatus(status)
                .map(statusMapper::toDto);
    }

    @Operation(summary = STATUS.DESCRIPTION_DELETE)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Integer id) {
        log.info(STATUS.DESCRIPTION_DELETE);
        return statusUseCase.deleteStatus(id);
    }
}