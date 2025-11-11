package com.moveo.ha.dto.project;

import com.moveo.ha.dto.task.TaskSummaryDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Collection;

@Schema(name = "ProjectResponse", description = "Project resource")
public record ProjectResponseDTO(
        @Schema(description = "Project ID", example = "1")
        Long id,

        @Schema(description = "Project name", example = "Website Redesign")
        String name,

        @Schema(description = "Project description", example = "Marketing site redesign for Q4")
        String description,

        @Schema(description = "Creation timestamp (UTC)", example = "2025-11-09T15:12:03Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp (UTC)", example = "2025-11-09T16:01:44Z")
        Instant updatedAt,

        @Schema(description = "Tasks (summary only)")
        Collection<TaskSummaryDTO> tasks
) {}
