package co.com.jhompo.usecase.loanapplication;

import co.com.jhompo.model.loanapplication.LoanApplication;
import co.com.jhompo.model.loanapplication.gateways.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class LoanApplicationUseCase {

    private final LoanApplicationRepository loanApplicationRepository;


    public Mono<LoanApplication> create(LoanApplication loanApplication) {
        return loanApplicationRepository.save(loanApplication);
    }

    public Mono<LoanApplication> findById(UUID id) {
        return loanApplicationRepository.findById(id);
    }
}