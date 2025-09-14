package co.com.jhompo.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ApplicationTypeDTO {

    private Integer id;
    private String name;
    private BigDecimal minimum_amount;
    private BigDecimal maximum_amount;
    private double interest_rate;
    private boolean automatic_validation;
}
