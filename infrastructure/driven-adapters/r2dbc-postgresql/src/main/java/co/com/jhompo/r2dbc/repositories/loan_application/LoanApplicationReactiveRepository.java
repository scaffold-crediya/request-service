package co.com.jhompo.r2dbc.repositories.loan_application;

import co.com.jhompo.r2dbc.entity.LoanApplicationEntity;
import co.com.jhompo.r2dbc.repositories.loan_application.projection.LoanSummaryProjection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

// This file is just an example, you should delete or modify it
public interface LoanApplicationReactiveRepository
        extends ReactiveCrudRepository<LoanApplicationEntity, UUID>, ReactiveQueryByExampleExecutor<LoanApplicationEntity> {

    @Query("SELECT la.* FROM application la JOIN statuses s ON la.id_status = s.id WHERE s.name = :statusName")
    Flux<LoanApplicationEntity> findByStatusName(@Param("statusName") String statusName, PageRequest pageRequest);



    @Query("""
        SELECT
            la.amount, la.term, la.email,
            lt.name loantypename, lt.interest_rate AS interest,
            s.name statusname,
            (
                SELECT COALESCE(SUM(a.amount), 0)
                FROM application a
                JOIN statuses sa ON a.id_status = sa.id
                WHERE a.email = la.email
                  AND sa.name = 'APROBADO'
            ) AS totalapproveddebt
        FROM application la
        JOIN statuses s ON la.id_status = s.id
        JOIN loan_type lt ON la.id_loan_type = lt.id
        WHERE s.name = :statusName
        LIMIT :size OFFSET :offset
    """)
    Flux<LoanSummaryProjection> findSummariesByStatus(@Param("statusName") String statusName,
                                                      @Param("size") int size,
                                                      @Param("offset") long offse);

}
