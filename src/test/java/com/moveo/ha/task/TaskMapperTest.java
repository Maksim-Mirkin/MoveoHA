package com.moveo.ha.task;

import com.moveo.ha.dto.task.TaskRequestDTO;
import com.moveo.ha.entity.Task;
import com.moveo.ha.entity.Project;
import com.moveo.ha.enums.TaskStatus;
import com.moveo.ha.mapper.TaskMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class TaskMapperTest {

    private final TaskMapper mapper = Mappers.getMapper(TaskMapper.class);

    @Test
    void toEntity_copiesFields_andIgnoresIdTimestampsAndProject() {
        var req = new TaskRequestDTO(1L, "Task", "Desc", TaskStatus.TODO);
        var entity = mapper.toEntity(req);

        assertEquals("Task", entity.getTitle());
        assertEquals("Desc", entity.getDescription());
        assertEquals(TaskStatus.TODO, entity.getStatus());
        assertNull(entity.getId());
        assertNull(entity.getProject());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }

    @Test
    void toResponse_copiesAllFields_andEmbedsProjectSummary() {
        var project = Project.builder()
                .id(10L).name("P").description("PD")
                .updatedAt(Instant.parse("2025-11-11T10:00:00Z"))
                .build();

        var task = Task.builder()
                .id(100L)
                .title("Task A")
                .description("Desc A")
                .status(TaskStatus.IN_PROGRESS)
                .project(project)
                .createdAt(Instant.parse("2025-11-11T10:00:00Z"))
                .updatedAt(Instant.parse("2025-11-11T11:00:00Z"))
                .build();

        var dto = mapper.toResponse(task);

        assertEquals(100L, dto.id());
        assertEquals("Task A", dto.title());
        assertEquals("Desc A", dto.description());
        assertEquals(TaskStatus.IN_PROGRESS, dto.status());
        assertNotNull(dto.project());
        assertEquals(10L, dto.project().id());
        assertEquals("P", dto.project().name());
        assertEquals(Instant.parse("2025-11-11T10:00:00Z"), dto.project().updatedAt());
    }
}
