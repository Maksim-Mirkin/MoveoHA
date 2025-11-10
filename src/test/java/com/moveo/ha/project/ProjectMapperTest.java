package com.moveo.ha.project;


import com.moveo.ha.dto.project.ProjectRequestDTO;
import com.moveo.ha.entity.Project;
import com.moveo.ha.mapper.ProjectMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class ProjectMapperTest {

    private final ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);

    @Test
    void toEntity_copiesNameAndDescription_andIgnoresIdAndTimestamps() {
        var req = new ProjectRequestDTO("N", "D");
        var entity = mapper.toEntity(req);
        assertEquals("N", entity.getName());
        assertEquals("D", entity.getDescription());
        assertNull(entity.getId());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }

    @Test
    void toResponse_copiesAll() {
        var entity = Project.builder()
                .id(10L).name("N").description("D")
                .build();
        var dto = mapper.toResponse(entity);
        assertEquals(10L, dto.id());
        assertEquals("N", dto.name());
        assertEquals("D", dto.description());
    }
}

