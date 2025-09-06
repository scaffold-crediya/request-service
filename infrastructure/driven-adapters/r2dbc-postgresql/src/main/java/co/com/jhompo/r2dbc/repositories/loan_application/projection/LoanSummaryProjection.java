package co.com.jhompo.r2dbc.repositories.loan_application.projection;

import java.math.BigDecimal;

public record LoanSummaryProjection(
        BigDecimal amount,
        Integer term,
        String email,
        String loantypename, // Coincide con el alias `lt.name AS loanTypeName`
        Double interest, // Coincide con el alias `lt.interest_rate AS interestRate`
        String statusname,   // Coincide con el alias `s.name AS statusName`
        BigDecimal totalapproveddebt
) {}