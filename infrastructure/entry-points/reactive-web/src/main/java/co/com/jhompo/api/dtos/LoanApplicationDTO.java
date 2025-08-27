package co.com.jhompo.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationDTO {

    private UUID id;
    private BigDecimal amount;
    private Integer term;
    private String email;
    private UUID statusId;
    private UUID applicationTypeId;
}