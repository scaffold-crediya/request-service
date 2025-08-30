package co.com.jhompo.model.status;
import lombok.*;
//import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Status {
    private int id;
    private String name;
    private String description;
}
