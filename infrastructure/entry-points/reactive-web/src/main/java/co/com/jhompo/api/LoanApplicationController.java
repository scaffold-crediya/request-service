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

import static co.com.jhompo.common.Messages.HTTP.*;
import static co.com.jhompo.common.Messages.*;
import static org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/solicitud")
@Tag(name = LOAN_APPLICATION.TITLE, description = LOAN_APPLICATION.DESCRIPTION )
public class LoanApplicationController {

    private final LoanApplicationUseCase loanApplicationUseCase;
    private final LoanApplicationMapper mapper;

    @Operation(summary = LOAN_APPLICATION.DESCRIPTION_CREATE)
    @PostMapping("/registrar")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<LoanApplication> create(@RequestBody LoanApplicationDTO dto) {
        log.info(LOAN_APPLICATION.DESCRIPTION_CREATE, dto);

        return getContext()
                .map(securityContext -> securityContext.getAuthentication())
                .map(Authentication::getPrincipal)
                .cast(String.class)
                .flatMap(authenticatedEmail -> {
                    if (!authenticatedEmail.equals(dto.getEmail())) {
                        return Mono.error(new IllegalArgumentException(LOAN_APPLICATION.CANNOT_CREATE_REQUEST_FOR_OTHER_USER));
                    }
                    return loanApplicationUseCase.create(mapper.toEntityForCreate(dto));
                })
                .doOnSuccess(app -> log.info(LOAN_APPLICATION.CREATED_SUCCESS, app))
                .doOnError(error -> log.error(LOAN_APPLICATION.CREATION_FAILED, error));
    }

    @Operation(summary = LOAN_APPLICATION.DESCRIPTION_UPDATE)
    @PutMapping("/{id}")
    public Mono<LoanApplication> update(@PathVariable(value = "id") UUID id, @RequestBody LoanApplication dto) {
        log.info(LOAN_APPLICATION.DESCRIPTION_UPDATE, id, dto);
        dto.setId(id);
        return loanApplicationUseCase.update(dto)
                .doOnSuccess(app -> log.info(LOAN_APPLICATION.UPDATED_SUCCESS, app))
                .doOnError(error -> log.error(LOAN_APPLICATION.UPDATE_FAILED, id, error));
    }

    @Operation(summary = LOAN_APPLICATION.DESCRIPTION_GET_ALL)
    @GetMapping
    public Flux<LoanApplication> getAll() {
        log.info(LOAN_APPLICATION.DESCRIPTION_GET_ALL);
        return loanApplicationUseCase.getAll()
                .doOnComplete(() -> log.info(LOAN_APPLICATION.FOUND_SUCCESS))
                .doOnError(error -> log.error(LOAN_APPLICATION.LIST_ERROR, error));
    }

    @Operation(summary = LOAN_APPLICATION.DESCRIPTION_FIND_BY_ID)
    @GetMapping("/{id}")
    public Mono<LoanApplication> getById(@PathVariable(value = "id") UUID id) {
        log.info(LOAN_APPLICATION.DESCRIPTION_FIND_BY_ID, id);
        return loanApplicationUseCase.getById(id)
                .doOnSuccess(app -> log.info(LOAN_APPLICATION.FOUND_SUCCESS, app))
                .doOnError(error -> log.error(LOAN_APPLICATION.NOT_FOUND, id, error));
    }

    @Operation(summary = LOAN_APPLICATION.DESCRIPTION_DELETE)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable(value = "id") UUID id) {
        log.info(LOAN_APPLICATION.DESCRIPTION_DELETE, id);
        return loanApplicationUseCase.delete(id)
                .doOnSuccess(v -> log.info(LOAN_APPLICATION.DELETED_SUCCESS, id))
                .doOnError(error -> log.error(LOAN_APPLICATION.DELETE_FAILED, id, error));
    }


    @Operation(summary = LOAN_APPLICATION.DESCRIPTION_GET_SUMMARY)
    @GetMapping("/estado/{statusName}")
    public Flux<LoanApplicationSummaryDTO> getSummariesByStatus( @PathVariable("statusName") String statusName,
                                                                 @RequestParam(name = "page", defaultValue = "0") int page,
                                                                 @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info(LOAN_APPLICATION.DESCRIPTION_GET_SUMMARY, statusName);
        return loanApplicationUseCase.findByStatusName(statusName, page, size);
    }
}