package com.moveo.ha.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Paginated list of tasks with paging & sorting metadata.")
public class TaskListDTO {

    @Schema(description = "Total number of tasks across all pages.")
    private long totalTasks;

    @Schema(description = "Current page number (zero-based).")
    private int pageNumber;

    @Schema(description = "Page size (elements per page).")
    private int pageSize;

    @Schema(description = "Total number of pages.")
    private int totalPages;

    @Schema(description = "Whether this page is the first one.")
    private boolean first;

    @Schema(description = "Whether this page is the last one.")
    private boolean last;

    @Schema(description = "Primary sort direction (asc/desc).")
    private String sortDir;

    @Schema(description = "Primary sort field.")
    private String sortBy;

    @Schema(description = "Tasks for the current page.")
    private Collection<TaskResponseDTO> tasks;
}
