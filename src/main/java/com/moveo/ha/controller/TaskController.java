package com.moveo.ha.controller;

import com.moveo.ha.dto.PageParams;
import com.moveo.ha.dto.task.TaskListDTO;
import com.moveo.ha.dto.task.TaskRequestDTO;
import com.moveo.ha.dto.task.TaskResponseDTO;
import com.moveo.ha.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Set;

@Tag(name = "Tasks", description = "Task management endpoints")
@RestController
@RequestMapping(path = "/api/v1/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Validated
public class TaskController {

    private final TaskService taskService;

    @Operation(
            summary = "Create task",
            description = "Creates a new task under a given project.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            content = @Content(schema = @Schema(implementation = TaskResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
            }
    )
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskResponseDTO> createTask(@Valid @RequestBody TaskRequestDTO body) {
        var created = taskService.createTask(body);
        var location = URI.create("/api/v1/tasks/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @Operation(
            summary = "Update task by id",
            description = "Updates title/description/status and optionally moves the task to another project.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = TaskResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Task not found", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
            }
    )
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskResponseDTO> updateTaskById(
            @Parameter(description = "Task id", example = "100") @PathVariable Long id,
            @Valid @RequestBody TaskRequestDTO body
    ) {
        var updated = taskService.updateTaskById(id, body);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Get task by id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = TaskResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Task not found", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
            }
    )
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getTaskById(
            @Parameter(description = "Task id", example = "100") @PathVariable Long id
    ) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @Operation(
            summary = "Get page of tasks",
            description = "Returns a paginated list of tasks.",
            parameters = {
                    @Parameter(name = "pageNumber", in = ParameterIn.QUERY, description = "Zero-based page index", example = "0"),
                    @Parameter(name = "pageSize", in = ParameterIn.QUERY, description = "Page size", example = "20"),
                    @Parameter(name = "sortBy", in = ParameterIn.QUERY, description = "Sort field", example = "id"),
                    @Parameter(name = "sortDir", in = ParameterIn.QUERY, description = "Sort direction (asc|desc)", example = "asc")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = TaskListDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
            }
    )
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping
    public TaskListDTO getPageOfTasks(@Valid @ParameterObject PageParams params) {
        var allowedSort = Set.of("id", "title", "status", "project", "createdAt", "updatedAt");
        var pageable = params.toPageable(allowedSort);
        return taskService.getPageOfTasks(pageable);
    }

    @Operation(
            summary = "Delete task by id",
            description = "Deletes a task and returns its snapshot.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = TaskResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Task not found", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> deleteTaskById(
            @Parameter(description = "Task id", example = "100") @PathVariable Long id
    ) {
        return ResponseEntity.ok(taskService.deleteTaskById(id));
    }
}

