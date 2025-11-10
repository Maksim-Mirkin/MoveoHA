package com.moveo.ha.service;

import com.moveo.ha.dto.project.ProjectListDTO;
import com.moveo.ha.dto.project.ProjectRequestDTO;
import com.moveo.ha.dto.project.ProjectResponseDTO;
import com.moveo.ha.error.NotFoundException;
import org.springframework.data.domain.Pageable;

/**
 * Application service for managing {@code Project} resources.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Create and update project data</li>
 *   <li>Retrieve single projects and paged lists</li>
 *   <li>Delete projects and return a snapshot of deleted data</li>
 * </ul>
 */
public interface ProjectService {

    /**
     * Create a new project.
     *
     * @param request DTO with project fields (name, description)
     * @return created project as DTO
     * @throws IllegalArgumentException if input fields are invalid.
     */
    ProjectResponseDTO createProject(ProjectRequestDTO request);

    /**
     * Update an existing project by id.
     *
     * @param id      project id
     * @param request DTO with updated fields
     * @return updated project as DTO
     * @throws NotFoundException if the project does not exist
     */
    ProjectResponseDTO updateProjectById(Long id, ProjectRequestDTO request);

    /**
     * Get a single project by id.
     *
     * @param id project id
     * @return project as DTO
     * @throws NotFoundException if the project does not exist
     */
    ProjectResponseDTO getProjectById(Long id);

    /**
     * Get a paginated list of projects.
     *
     * @param pageable Spring Data pageable (page, size, sort)
     * @return page DTO with pagination metadata and project items
     * @throws IllegalArgumentException if paging/sorting parameters are invalid or not allowed
     */
    ProjectListDTO getPageOfProjects(Pageable pageable);

    /**
     * Delete a project by id and return a snapshot of what was deleted.
     *
     * @param id project id
     * @return deleted project as DTO
     * @throws NotFoundException if the project does not exist
     */
    ProjectResponseDTO deleteProjectById(Long id);
}
