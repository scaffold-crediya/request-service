package co.com.jhompo.api;
import co.com.jhompo.api.dtos.LoanApplicationDTO;
import co.com.jhompo.api.dtos.status.StatusDTO;
import co.com.jhompo.api.dtos.status.StatusRequestDTO;
import co.com.jhompo.api.mapper.StatusMapper;
import co.com.jhompo.usecase.status.StatusUseCase;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/statuses")
@RequiredArgsConstructor
public class StatusController {

    private final StatusUseCase statusUseCase;
    private final StatusMapper statusMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<StatusDTO> create(@RequestBody StatusRequestDTO requestDTO) {

        var status = statusMapper.toDomain(0, requestDTO);  // El ID '0' es un placeholder, ya que la base de datos lo generará.
        return statusUseCase.createStatus(status)
                .map(statusMapper::toDto);
    }

    @GetMapping
    public Flux<StatusDTO> getAll() {
        return statusUseCase.getAllStatuses()
                .map(statusMapper::toDto);
    }



    @GetMapping("/{id}")
    public Mono<StatusDTO> getById(@PathVariable Integer id) {
        return statusUseCase.getStatusById(id)
                .map(statusMapper::toDto);
    }

    @PutMapping("/{id}")
    public Mono<StatusDTO> update(@PathVariable Integer id, @RequestBody StatusRequestDTO requestDTO) {
        var status = statusMapper.toDomain(id, requestDTO);
        return statusUseCase.updateStatus(status)
                .map(statusMapper::toDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Integer id) {
        return statusUseCase.deleteStatus(id);
    }
}
