package co.com.jhompo.api.mapper;

import co.com.jhompo.api.dtos.LoanApplicationDTO;
import co.com.jhompo.model.loanapplication.LoanApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanApplicationMapper {

    // Crear: no mapea id
    @Mapping(target = "id", ignore = true)
    LoanApplication toEntityForCreate(LoanApplicationDTO dto);

    // A DTO
    LoanApplicationDTO toDto(LoanApplication entity);
}