package co.com.jhompo.api.mapper;

import co.com.jhompo.api.dtos.status.StatusDTO;
import co.com.jhompo.api.dtos.status.StatusRequestDTO;
import co.com.jhompo.model.status.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class StatusMapperTest {

    private StatusMapper statusMapper;

    @BeforeEach
    void setUp() {
        statusMapper = new StatusMapper();
    }

    @Test
    void shouldMapRequestDtoToDomain() {
        // Given
        int id = 1;
        StatusRequestDTO requestDto = new StatusRequestDTO("Active", "Status activo");

        // When
        Status result = statusMapper.toDomain(id, requestDto);

        // Then
        assertEquals(id, result.getId());
        assertEquals("Active", result.getName());
        assertEquals("Status activo", result.getDescription());
    }

    @Test
    void shouldMapDomainToDto() {
        // Given
        Status status = new Status(2, "Inactive", "Status inactivo");

        // When
        StatusDTO result = statusMapper.toDto(status);

        // Then
        assertEquals(2, result.id());
        assertEquals("Inactive", result.name());
        assertEquals("Status inactivo", result.description());
    }
}