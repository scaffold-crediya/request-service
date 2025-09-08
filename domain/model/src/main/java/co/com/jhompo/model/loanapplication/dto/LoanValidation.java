package co.com.jhompo.model.loanapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanValidation {
    private String loanId;
    private String userName;
    private String email;
    private Double amount;
    private Integer term;
    private Double interestRate;
    private Double baseSalary;
    private List<Double> activeDebts;
    private Integer requestedStatusId;
    private Integer loanTypeId;
    private Boolean automaticValidation;
}
