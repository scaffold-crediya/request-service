package co.com.jhompo.model.loantype;
import lombok.*;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanType {
    private int id;
    private String name;
    private BigDecimal minimum_amount;
    private BigDecimal maximum_amount;
    private double interest_rate;
    private boolean automatic_validation;
}
