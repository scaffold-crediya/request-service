package co.com.jhompo.model.loanapplication;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplication {

    private UUID id;
    private BigDecimal amount;
    private Integer term;
    private String email;
    private Integer statusId;
    private Integer applicationTypeId;


}