package com.moveo.ha.dto.task;

import com.moveo.ha.dto.project.ProjectSummaryDTO;
import com.moveo.ha.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "TaskResponseDTO", description = "Task resource")
public record TaskResponseDTO(

        @Schema(description = "Task ID", example = "100")
        Long id,

        @Schema(description = "Task title", example = "Prepare sprint demo")
        String title,

        @Schema(description = "Task description")
        String description,

        @Schema(description = "Task status", example = "IN_PROGRESS")
        TaskStatus status,

        @Schema(description = "Creation timestamp UTC", example = "2025-11-10T15:00:00Z")
        Instant createdAt,

        @Schema(description = "Update timestamp UTC", example = "2025-11-10T15:12:00Z")
        Instant updatedAt,

        @Schema(description = "Owner project (summary only)")
        ProjectSummaryDTO project
) {}
