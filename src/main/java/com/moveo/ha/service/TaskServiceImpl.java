package com.moveo.ha.service;

import com.moveo.ha.dto.task.TaskListDTO;
import com.moveo.ha.dto.task.TaskRequestDTO;
import com.moveo.ha.dto.task.TaskResponseDTO;
import com.moveo.ha.error.BadRequestException;
import com.moveo.ha.error.NotFoundException;
import com.moveo.ha.mapper.TaskMapper;
import com.moveo.ha.repository.ProjectRepository;
import com.moveo.ha.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@inheritDoc}
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TaskMapper taskMapper;

    /** {@inheritDoc} */
    @Override
    public TaskResponseDTO createTask(TaskRequestDTO dto) {
        log.info("Creating task for projectId={} title='{}'", dto.projectId(), dto.title());

        var project = projectRepository.findById(dto.projectId()).orElseThrow(() -> {
            log.warn("Create failed: project id={} not found", dto.projectId());
            return new NotFoundException("Project %d not found".formatted(dto.projectId()));
        });

        var entity = taskMapper.toEntity(dto);
        entity.setProject(project);

        var saved = taskRepository.save(entity);
        var resp = taskMapper.toResponse(saved);

        log.info("Created task id={} under projectId={} status={}", resp.id(), project.getId(), resp.status());
        return resp;
    }

    /** {@inheritDoc} */
    @Override
    public TaskResponseDTO updateTaskById(Long id, TaskRequestDTO dto) {
        log.info("Updating task id={} ...", id);

        var task = taskRepository.findById(id).orElseThrow(() -> {
            log.warn("Update failed: task id={} not found", id);
            return new NotFoundException("Task %d not found".formatted(id));
        });

        if (!task.getProject().getId().equals(dto.projectId())) {
            var newProject = projectRepository.findById(dto.projectId()).orElseThrow(() -> {
                log.warn("Update failed: target project id={} not found", dto.projectId());
                return new NotFoundException("Project %d not found".formatted(dto.projectId()));
            });
            task.setProject(newProject);
        }

        task.setTitle(dto.title());
        task.setDescription(dto.description());
        task.setStatus(dto.status());

        var updated = taskRepository.save(task);
        var resp = taskMapper.toResponse(updated);

        log.info("Updated task id={} -> title='{}', status={}, projectId={}",
                id, resp.title(), resp.status(), resp.project() != null ? resp.project().id() : null);
        return resp;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public TaskResponseDTO getTaskById(Long id) {
        log.debug("Fetching task id={}", id);
        return taskRepository.findById(id)
                .map(e -> {
                    var dto = taskMapper.toResponse(e);
                    log.debug("Fetched task id={} (title='{}')", id, dto.title());
                    return dto;
                })
                .orElseThrow(() -> {
                    log.warn("Get failed: task id={} not found", id);
                    return new NotFoundException("Task %d not found".formatted(id));
                });
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public TaskListDTO getPageOfTasks(Pageable pageable) {
        log.debug("Listing tasks page={} size={} sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        var page = taskRepository.findAll(pageable).map(taskMapper::toResponse);

        if (pageable.getPageNumber() >= page.getTotalPages() && page.getTotalPages() > 0) {
            log.warn("Requested page {} is out of range (totalPages={})",
                    pageable.getPageNumber(), page.getTotalPages());
            throw new BadRequestException("This page does not exist.");
        }

        String sortBy = "id";
        String sortDir = "asc";
        var it = pageable.getSort().iterator();
        if (it.hasNext()) {
            var order = it.next();
            sortBy = order.getProperty();
            sortDir = order.getDirection().name().toLowerCase();
        }

        var dto = TaskListDTO.builder()
                .totalTasks(page.getTotalElements())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .sortBy(sortBy)
                .sortDir(sortDir)
                .tasks(page.getContent())
                .build();

        log.debug("Listed tasks: totalElements={} totalPages={}",
                page.getTotalElements(), page.getTotalPages());
        return dto;
    }

    /** {@inheritDoc} */
    @Override
    public TaskResponseDTO deleteTaskById(Long id) {
        log.info("Deleting task id={} ...", id);

        var task = taskRepository.findById(id).orElseThrow(() -> {
            log.warn("Delete failed: task id={} not found", id);
            return new NotFoundException("Task %d not found".formatted(id));
        });

        var snapshot = taskMapper.toResponse(task);
        taskRepository.delete(task);

        log.info("Deleted task id={} (title='{}')", id, snapshot.title());
        return snapshot;
    }
}
