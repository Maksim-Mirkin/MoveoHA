package com.moveo.ha.dto.task;

import com.moveo.ha.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Lightweight summary view of a Task (for embedding)")
public record TaskSummaryDTO(
        @Schema(description = "Task ID")
        Long id,
        @Schema(description = "Task title")
        String title,
        @Schema(description = "Task status")
        TaskStatus status,
        @Schema(description = "Last update timestamp")
        Instant updatedAt
) {}
