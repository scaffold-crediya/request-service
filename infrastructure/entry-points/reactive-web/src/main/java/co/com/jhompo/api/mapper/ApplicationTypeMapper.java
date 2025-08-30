package co.com.jhompo.api.mapper;

import co.com.jhompo.api.dtos.ApplicationTypeDTO;
import co.com.jhompo.model.applicationtype.ApplicationType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApplicationTypeMapper {

    ApplicationTypeDTO toDto(ApplicationType applicationType);
    ApplicationType toEntity(ApplicationTypeDTO applicationTypeDTO);
}