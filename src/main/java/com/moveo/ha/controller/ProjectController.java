package com.moveo.ha.controller;

import com.moveo.ha.dto.PageParams;
import com.moveo.ha.dto.project.ProjectListDTO;
import com.moveo.ha.dto.project.ProjectRequestDTO;
import com.moveo.ha.dto.project.ProjectResponseDTO;
import com.moveo.ha.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Set;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/api/v1/projects")
@Tag(
        name = "Projects",
        description = "Project CRUD with paging and role-based access."
)
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;

    @Operation(
            summary = "Create project (ADMIN)",
            description = "Creates a new project. ADMIN only.",
            operationId = "createProject",
            requestBody = @RequestBody(
                    required = true,
                    description = "Payload to create a project",
                    content = @Content(
                            schema = @Schema(implementation = ProjectRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "Create",
                                    value = """
                                            {
                                              "name": "Website Redesign",
                                              "description": "Marketing site redesign for Q4"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Created. Location header points to the new resource.",
                            content = @Content(schema = @Schema(implementation = ProjectResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Created",
                                            value = """
                                                    {
                                                      "id": 1,
                                                      "name": "Website Redesign",
                                                      "description": "Marketing site redesign for Q4",
                                                      "createdAt": "2025-11-09T15:12:03Z",
                                                      "updatedAt": "2025-11-09T15:12:03Z"
                                                    }
                                                    """
                                    ))
                    ),
                    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Missing/invalid JWT", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden (ADMIN required)", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ProjectResponseDTO> createProject(@Valid @org.springframework.web.bind.annotation.RequestBody ProjectRequestDTO request) {
        var dto = projectService.createProject(request);
        return ResponseEntity
                .created(URI.create("/api/v1/projects/" + dto.id()))
                .body(dto);
    }

    @Operation(
            summary = "Update project by id (ADMIN)",
            description = "Updates an existing project by ID. ADMIN only.",
            operationId = "updateProjectById",
            requestBody = @RequestBody(
                    required = true,
                    description = "Payload to update a project",
                    content = @Content(
                            schema = @Schema(implementation = ProjectRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "Update",
                                    value = """
                                            {
                                              "name": "Website Rebrand",
                                              "description": "Scope updated to Q4-Q1"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = ProjectResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Validation/type error", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Missing/invalid JWT", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden (ADMIN required)", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Project not found", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}")
    public ProjectResponseDTO updateProjectById(
            @Parameter(description = "Project ID", example = "1") @PathVariable Long id,
            @Valid @org.springframework.web.bind.annotation.RequestBody ProjectRequestDTO request
    ) {
        return projectService.updateProjectById(id, request);
    }

    @Operation(
            summary = "Get project by id (ADMIN/USER)",
            description = "Returns a project by ID. Accessible by ADMIN and USER.",
            operationId = "getProjectById",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(schema = @Schema(implementation = ProjectResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Found",
                                            value = """
                                                    {
                                                      "id": 1,
                                                      "name": "Website Redesign",
                                                      "description": "Marketing site redesign for Q4",
                                                      "createdAt": "2025-11-09T15:12:03Z",
                                                      "updatedAt": "2025-11-09T16:01:44Z"
                                                    }
                                                    """
                                    ))
                    ),
                    @ApiResponse(responseCode = "401", description = "Missing/invalid JWT", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Project not found", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
            }
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ProjectResponseDTO getProjectById(
            @Parameter(description = "Project ID", example = "1") @PathVariable Long id
    ) {
        return projectService.getProjectById(id);
    }

    @Operation(
            summary = "Get paged projects (ADMIN/USER)",
            description = "Returns a paged list of projects. Supports PageParams query fields.",
            operationId = "getPageOfProjects",
            parameters = {
                    @Parameter(name = "pageNumber", description = "Zero-based page index", example = "0"),
                    @Parameter(name = "pageSize", description = "Page size (1..200)", example = "10"),
                    @Parameter(name = "sortBy", description = "Sort field (id|name|createdAt|updatedAt)", example = "name"),
                    @Parameter(name = "sortDir", description = "Sort direction (asc|desc)", example = "asc")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(
                                    schema = @Schema(implementation = ProjectListDTO.class),
                                    examples = @ExampleObject(
                                            name = "Page",
                                            value = """
                                                    {
                                                      "totalProjects": 42,
                                                      "pageNumber": 0,
                                                      "pageSize": 10,
                                                      "totalPages": 5,
                                                      "isFirst": true,
                                                      "isLast": false,
                                                      "sortDir": "asc",
                                                      "sortBy": "name",
                                                      "projects": [
                                                        {
                                                          "id": 1,
                                                          "name": "Website Redesign",
                                                          "description": "Marketing site redesign for Q4",
                                                          "createdAt": "2025-11-09T15:12:03Z",
                                                          "updatedAt": "2025-11-09T16:01:44Z"
                                                        }
                                                      ]
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid paging/sorting parameters", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Missing/invalid JWT", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
            }
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ProjectListDTO getPageOfProjects(@Valid @ParameterObject PageParams params) {
        var allowedSort = Set.of("id", "name", "createdAt", "updatedAt");
        var pageable = params.toPageable(allowedSort);
        return projectService.getPageOfProjects(pageable);
    }

    @Operation(
            summary = "Delete project by id (ADMIN)",
            description = "Deletes a project by ID and returns a snapshot of the deleted resource. ADMIN only.",
            operationId = "deleteProjectById",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Deleted. Returns the deleted project DTO.",
                            content = @Content(schema = @Schema(implementation = ProjectResponseDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid paging/sorting parameters (page out of range, invalid sort field, etc.)"
                    ),
                    @ApiResponse(responseCode = "401", description = "Missing/invalid JWT", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden (ADMIN required)", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Project not found", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ProjectResponseDTO deleteProjectById(
            @Parameter(description = "Project ID", example = "1") @PathVariable Long id
    ) {
        return projectService.deleteProjectById(id);
    }
}
