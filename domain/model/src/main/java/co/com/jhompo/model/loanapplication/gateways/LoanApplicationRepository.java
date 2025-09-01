package co.com.jhompo.model.loanapplication.gateways;

import co.com.jhompo.model.loanapplication.LoanApplication;
import co.com.jhompo.model.loanapplication.dto.LoanApplicationSummaryDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface LoanApplicationRepository {

    Mono<LoanApplication> save(LoanApplication loanApplication);

    Mono<LoanApplication> findById(UUID id);

    Flux<LoanApplication> findAll();

    Flux<LoanApplicationSummaryDTO> findSummariesByStatus(String statusName, int page, int size);

    Mono<Void> deleteById(UUID id);
}
