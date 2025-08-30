package co.com.jhompo.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table("loan_type")
public class ApplicationTypeEntity {
    @Id
    private int id;
    private String name;
    private double minimum_amount;
    private double maximum_amount;
    private double interest_rate;
    private boolean automatic_validation;
}