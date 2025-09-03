package co.com.jhompo.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationResponseDTO {

    private BigDecimal amount;
    private Integer term;
    private String email;
    private String statusName;
    private String loanTypeName;
}
