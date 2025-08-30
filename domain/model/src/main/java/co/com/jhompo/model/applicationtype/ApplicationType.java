package co.com.jhompo.model.applicationtype;
import lombok.*;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ApplicationType {
    private int id;
    private String name;
    private double minimum_amount;
    private double maximum_amount;
    private double interest_rate;
    private boolean automatic_validation;
}
