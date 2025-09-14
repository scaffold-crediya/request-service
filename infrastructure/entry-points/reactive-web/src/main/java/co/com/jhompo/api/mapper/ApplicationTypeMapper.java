package co.com.jhompo.api.mapper;

import co.com.jhompo.api.dtos.ApplicationTypeDTO;
import co.com.jhompo.model.loantype.LoanType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApplicationTypeMapper {

    ApplicationTypeDTO toDto(LoanType loanType);
    LoanType toEntity(ApplicationTypeDTO applicationTypeDTO);
}