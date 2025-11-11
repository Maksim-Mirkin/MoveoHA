package com.moveo.ha.dto.task;

import com.moveo.ha.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "TaskRequestDTO", description = "Payload to create/update a task")
public record TaskRequestDTO(

        @Schema(description = "Project ID which owns this Task", example = "12")
        @NotNull Long projectId,

        @Schema(description = "Task title", maxLength = 200, example = "Prepare sprint demo")
        @NotBlank @Size(max = 200) String title,

        @Schema(description = "Task description", maxLength = 2000,
                example = "Need slides, video, and talking points for stake holders")
        @NotBlank @Size(max = 2000) String description,

        @Schema(description = "Task status", example = "TODO")
        @NotNull TaskStatus status
) {}
