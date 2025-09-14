package co.com.jhompo.usecase.loantype;

import co.com.jhompo.model.loantype.LoanType;
import co.com.jhompo.util.Messages.*;
import co.com.jhompo.model.loantype.gateways.LoanTypeRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class LoanTypeUseCase {

    private final LoanTypeRepository repository;

    public Mono<LoanType> create(LoanType loanType) {

        return repository.save(loanType);
    }

    public Mono<LoanType> getApplicationTypeById(Integer id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(APPLICATION_TYPE.NOT_FOUND)));
    }

    public Flux<LoanType> getAllType() {
        return repository.findAll();
    }

    public Mono<LoanType> updateLoanType(LoanType aplicationType) {

        return repository.findById(aplicationType.getId())
                .switchIfEmpty(Mono.error(new RuntimeException(APPLICATION_TYPE.NOT_FOUND)))
                .flatMap(existingStatus -> repository.save(aplicationType));
    }

    public Mono<Void> deleteLoanType(Integer id) {
        return repository.deleteById(id);
    }
}
