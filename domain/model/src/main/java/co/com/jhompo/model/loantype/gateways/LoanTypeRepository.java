package co.com.jhompo.model.loantype.gateways;

import co.com.jhompo.model.loantype.LoanType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LoanTypeRepository {

    Mono<LoanType> save(LoanType status);
    Mono<LoanType> findById(Integer id);
    Flux<LoanType> findAll();
    Mono<Void> deleteById(Integer id);
    Mono<Boolean> existsByName(String name);
}
