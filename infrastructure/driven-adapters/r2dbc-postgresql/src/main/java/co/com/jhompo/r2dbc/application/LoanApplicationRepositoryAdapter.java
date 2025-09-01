package co.com.jhompo.r2dbc.application;

import co.com.jhompo.model.loanapplication.LoanApplication;
import co.com.jhompo.model.loanapplication.dto.LoanApplicationSummaryDTO;
import co.com.jhompo.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.jhompo.r2dbc.entity.LoanApplicationEntity;
import co.com.jhompo.r2dbc.helper.ReactiveAdapterOperations;
import co.com.jhompo.r2dbc.projection.LoanSummaryProjection;
import org.reactivecommons.utils.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public class LoanApplicationRepositoryAdapter
        extends ReactiveAdapterOperations<LoanApplication, LoanApplicationEntity, UUID, LoanApplicationReactiveRepository>
        implements LoanApplicationRepository
{
    public LoanApplicationRepositoryAdapter(LoanApplicationReactiveRepository repository, ObjectMapper mapper, TransactionalOperator transactionalOperator) {

        super(repository, mapper, d -> mapper.map(d, LoanApplication.class));
        this.transactionalOperator = transactionalOperator;
    }

    private final Logger log = LoggerFactory.getLogger(LoanApplicationRepositoryAdapter.class);
    private final TransactionalOperator transactionalOperator;

    @Override
    public Mono<LoanApplication> save(LoanApplication loan) {
        return transactionalOperator.transactional(
                super.save(loan)
                        .doOnSuccess(u -> log.info("Guardado en BD: {}", u))
                        .doOnError(e -> log.error("Error al guardar el usuario: {}", loan, e))
        );
    }


    @Override
    public Mono<Void> deleteById(UUID id) {
        return repository.deleteById(id);
    }

    @Override
    public Flux<LoanApplicationSummaryDTO> findSummariesByStatus(String statusName, int page, int size) {
        return repository.findSummariesByStatus(statusName, PageRequest.of(page, size))
                .map(this::toDto);
    }

    private LoanApplicationSummaryDTO toDto(LoanSummaryProjection projection) {
        return LoanApplicationSummaryDTO.builder()
                .amount(projection.amount())
                .term(projection.term())
                .email(projection.email())
                .loanTypeName(projection.loantypename())
                .interestRate(projection.interest())
                .statusName(projection.statusname())
                .totalApprovedDebt(projection.totalapproveddebt())
                .build();
    }
}
