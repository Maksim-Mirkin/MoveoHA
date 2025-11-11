package com.moveo.ha.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Lightweight summary view of a Project (for embedding)")
public record ProjectSummaryDTO(
        @Schema(description = "Project ID")
        Long id,
        @Schema(description = "Project name")
        String name,
        @Schema(description = "Last update timestamp")
        Instant updatedAt
) {}
