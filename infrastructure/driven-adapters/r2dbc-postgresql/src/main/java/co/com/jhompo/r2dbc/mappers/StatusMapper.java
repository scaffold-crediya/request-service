package co.com.jhompo.r2dbc.mappers;

import co.com.jhompo.model.status.Status;
import co.com.jhompo.r2dbc.entity.StatusEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StatusMapper {

     StatusEntity toEntity(Status status);

     Status toDomain(StatusEntity statusEntity);
}
