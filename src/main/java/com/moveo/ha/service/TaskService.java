package com.moveo.ha.service;

import com.moveo.ha.dto.task.TaskListDTO;
import com.moveo.ha.dto.task.TaskRequestDTO;
import com.moveo.ha.dto.task.TaskResponseDTO;
import com.moveo.ha.error.BadRequestException;
import com.moveo.ha.error.NotFoundException;
import org.springframework.data.domain.Pageable;

/**
 * Application service for managing {@code Task} domain objects.
 *
 * <p><b>Responsibilities</b>
 * <ul>
 *   <li>Create / update / delete a single task</li>
 *   <li>Fetch a single task</li>
 *   <li>Fetch a paginated list of tasks</li>
 * </ul>
 */
public interface TaskService {

    /**
     * Create a new task under the given project.
     *
     * @param dto request payload (must include {@code projectId}, {@code title}, {@code description}, {@code status})
     * @return created task snapshot
     * @throws NotFoundException if the project with {@code dto.projectId()} does not exist
     */
    TaskResponseDTO createTask(TaskRequestDTO dto);

    /**
     * Update an existing task by its id.
     * <p>You may modify {@code title}, {@code description}, {@code status}, and optionally move
     * the task to another project via {@code projectId}.</p>
     *
     * @param id  task id
     * @param dto update payload
     * @return updated task snapshot
     * @throws NotFoundException if the task with {@code id} does not exist,
     *                           or if the target project {@code dto.projectId()} does not exist
     */
    TaskResponseDTO updateTaskById(Long id, TaskRequestDTO dto);

    /**
     * Get a task by its id.
     *
     * @param id task id
     * @return task snapshot
     * @throws NotFoundException if the task does not exist
     */
    TaskResponseDTO getTaskById(Long id);

    /**
     * Get a paginated list of tasks with paging metadata.
     *
     * @param pageable pagination and sorting parameters
     * @return {@link TaskListDTO} containing metadata and current page items
     * @throws BadRequestException if the requested page index is out of range
     */
    TaskListDTO getPageOfTasks(Pageable pageable);

    /**
     * Delete a task by its id and return a snapshot of the removed entity.
     *
     * @param id task id
     * @return deleted task snapshot (useful for UI confirmations and audit logs)
     * @throws NotFoundException if the task does not exist
     */
    TaskResponseDTO deleteTaskById(Long id);
}
