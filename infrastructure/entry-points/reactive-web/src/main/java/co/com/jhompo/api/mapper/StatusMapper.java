package co.com.jhompo.api.mapper;

import co.com.jhompo.api.dtos.status.StatusDTO;
import co.com.jhompo.api.dtos.status.StatusRequestDTO;
import co.com.jhompo.model.status.Status;
import org.springframework.stereotype.Component;

@Component
public class StatusMapper {

    public Status toDomain(int id, StatusRequestDTO dto) {
        return new Status(id, dto.name(), dto.description());
    }

    public StatusDTO toDto(Status status) {
        return new StatusDTO(status.getId(), status.getName(), status.getDescription());
    }
}