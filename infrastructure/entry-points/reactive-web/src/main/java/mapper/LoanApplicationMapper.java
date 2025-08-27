package mapper;

import co.com.jhompo.api.dtos.LoanApplicationDTO;
import co.com.jhompo.model.loanapplication.LoanApplication;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;


public class LoanApplicationMapper {


    public static LoanApplication toDomain(LoanApplicationDTO dto) {
        // Valida que el DTO no sea nulo para evitar NullPointerException
        if (dto == null) {
            return null;
        }

        return new LoanApplication(
            dto.getId(),
            dto.getAmount(),
            dto.getTerm(),
            dto.getEmail(),
            dto.getStatusId(),
            dto.getApplicationTypeId()
        );
    }


    public static LoanApplicationDTO toDto(LoanApplication entity) {
        // Valida que la entidad no sea nula
        if (entity == null) {
            return null;
        }

        // El estado (Enum) se convierte a String para la respuesta JSON.
        return new LoanApplicationDTO(
                entity.getId(),
                entity.getAmount(),
                entity.getTerm(),
                entity.getEmail(),
                entity.getStatusId(),
                entity.getApplicationTypeId()
        );
    }
}
