package co.com.jhompo.model.loanapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationSummaryDTO {
    private BigDecimal amount;
    private Integer term;
    private String email;
    private String name;
    private String loanTypeName;
    private double interestRate;
    private String statusName;
    private BigDecimal baseSalary;
    private BigDecimal totalApprovedDebt;
    private Boolean automaticValidation; // nuevo
    private Integer loanTypeId;
}

