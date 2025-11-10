package com.moveo.ha.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "ProjectRequestDTO", description = "Payload to create/update a project")
public record ProjectRequestDTO(
        @Schema(description = "Project name", example = "Website Redesign", maxLength = 100)
        @NotBlank @Size(max = 100) String name,

        @Schema(description = "Project description", example = "Marketing site redesign for Q4", maxLength = 1000)
        @NotBlank @Size(max = 1000) String description
) {
}
