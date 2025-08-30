package co.com.jhompo.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("application")
public class LoanApplicationEntity  {
    @Id
    @Column("id")
    private UUID id;
    private BigDecimal amount;
    private Integer term;
    private String email;

    @Column("id_status")
    private Integer statusId;

    @Column("id_loan_type")
    private Integer applicationTypeId;

}







