package com.moveo.ha.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Paginated list of projects with paging & sorting metadata.")
public class ProjectListDTO {

    @Schema(description = "Total number of projects across all pages.", example = "100")
    private long totalProjects;

    @Schema(description = "Current page number (zero-based).", example = "0")
    private int pageNumber;

    @Schema(description = "Page size (elements per page).", example = "20")
    private int pageSize;

    @Schema(description = "Total number of pages.", example = "5")
    private int totalPages;

    @Schema(description = "Whether this page is the first one.", example = "true")
    private boolean first;

    @Schema(description = "Whether this page is the last one.", example = "false")
    private boolean last;

    @Schema(description = "Primary sort direction (asc/desc).", example = "asc")
    private String sortDir;

    @Schema(description = "Primary sort field.", example = "name")
    private String sortBy;

    @Schema(description = "Projects for the current page.")
    private Collection<ProjectResponseDTO> projects;
}
