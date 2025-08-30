package co.com.jhompo.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ApplicationTypeDTO {

    private Integer id;
    private String name;
    private double minimum_amount;
    private double maximum_amount;
    private double interest_rate;
    private boolean automatic_validation;
}
