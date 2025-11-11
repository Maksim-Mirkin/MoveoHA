package com.moveo.ha.task;

import com.moveo.ha.dto.project.ProjectSummaryDTO;
import com.moveo.ha.dto.task.TaskRequestDTO;
import com.moveo.ha.dto.task.TaskResponseDTO;
import com.moveo.ha.entity.Project;
import com.moveo.ha.entity.Task;
import com.moveo.ha.enums.TaskStatus;
import com.moveo.ha.error.BadRequestException;
import com.moveo.ha.error.NotFoundException;
import com.moveo.ha.mapper.TaskMapper;
import com.moveo.ha.repository.ProjectRepository;
import com.moveo.ha.repository.TaskRepository;
import com.moveo.ha.service.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TaskServiceImplTest {

    private TaskRepository taskRepo;
    private ProjectRepository projectRepo;
    private TaskMapper mapper;
    private TaskServiceImpl service;

    @BeforeEach
    void setUp() {
        taskRepo = mock(TaskRepository.class);
        projectRepo = mock(ProjectRepository.class);
        mapper = mock(TaskMapper.class);
        service = new TaskServiceImpl(taskRepo, projectRepo, mapper);
    }

    // ---------- helpers ----------
    private Project project(long id) {
        return Project.builder().id(id).name("P").description("PD").build();
    }
    private Task entity(long id, long projectId) {
        return Task.builder()
                .id(id).title("T").description("D").status(TaskStatus.TODO)
                .project(project(projectId))
                .createdAt(Instant.parse("2025-11-10T15:00:00Z"))
                .updatedAt(Instant.parse("2025-11-10T15:10:00Z"))
                .build();
    }
    private TaskResponseDTO dto(long id, long projectId) {
        return new TaskResponseDTO(
                id, "T", "D", TaskStatus.TODO,
                Instant.parse("2025-11-10T15:00:00Z"),
                Instant.parse("2025-11-10T15:10:00Z"),
                new ProjectSummaryDTO(projectId, "P", Instant.parse("2025-11-10T15:10:00Z"))
        );
    }

    // ---------- CREATE ----------
    @Test
    void createTask_ok() {
        var req = new TaskRequestDTO(1L, "T", "D", TaskStatus.TODO);
        var proj = project(1L);
        var toSave = Task.builder().title("T").description("D").status(TaskStatus.TODO).project(proj).build();
        var saved = entity(100L, 1L);
        var outDto = dto(100L, 1L);

        when(projectRepo.findById(1L)).thenReturn(Optional.of(proj));
        when(mapper.toEntity(req)).thenReturn(toSave);
        when(taskRepo.save(toSave)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(outDto);

        var out = service.createTask(req);
        assertThat(out).isEqualTo(outDto);
        verify(taskRepo).save(toSave);
    }

    @Test
    void createTask_projectNotFound() {
        var req = new TaskRequestDTO(99L, "T", "D", TaskStatus.TODO);
        when(projectRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createTask(req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Project 99 not found");
        verify(taskRepo, never()).save(any());
    }

    // ---------- UPDATE ----------
    @Test
    void updateTask_ok_sameProject() {
        var existing = entity(5L, 1L);
        var req = new TaskRequestDTO(1L, "T2", "D2", TaskStatus.IN_PROGRESS);
        var updated = existing.toBuilder().title("T2").description("D2").status(TaskStatus.IN_PROGRESS).build();
        var outDto = dto(5L, 1L);

        when(taskRepo.findById(5L)).thenReturn(Optional.of(existing));
        when(projectRepo.findById(1L)).thenReturn(Optional.of(project(1L)));
        when(taskRepo.save(existing)).thenReturn(updated);
        when(mapper.toResponse(updated)).thenReturn(outDto);

        var out = service.updateTaskById(5L, req);
        assertThat(out).isEqualTo(outDto);
        assertThat(existing.getTitle()).isEqualTo("T2");
        assertThat(existing.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void updateTask_changeProject_ok() {
        var existing = entity(7L, 1L);
        var req = new TaskRequestDTO(2L, "T", "D", TaskStatus.TODO);

        when(taskRepo.findById(7L)).thenReturn(Optional.of(existing));
        when(projectRepo.findById(2L)).thenReturn(Optional.of(project(2L)));
        when(taskRepo.save(existing)).thenReturn(existing);
        when(mapper.toResponse(existing)).thenReturn(dto(7L, 2L));

        var out = service.updateTaskById(7L, req);
        assertThat(out.project().id()).isEqualTo(2L);
    }

    @Test
    void updateTask_taskNotFound() {
        when(taskRepo.findById(111L)).thenReturn(Optional.empty());
        var req = new TaskRequestDTO(1L, "T", "D", TaskStatus.TODO);
        assertThatThrownBy(() -> service.updateTaskById(111L, req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Task 111 not found");
    }

    @Test
    void updateTask_newProjectNotFound() {
        var existing = entity(8L, 1L);
        when(taskRepo.findById(8L)).thenReturn(Optional.of(existing));
        when(projectRepo.findById(777L)).thenReturn(Optional.empty());

        var req = new TaskRequestDTO(777L, "T", "D", TaskStatus.TODO);
        assertThatThrownBy(() -> service.updateTaskById(8L, req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Project 777 not found");
    }

    // ---------- GET BY ID ----------
    @Test
    void getTaskById_ok() {
        var e = entity(10L, 1L);
        when(taskRepo.findById(10L)).thenReturn(Optional.of(e));
        when(mapper.toResponse(e)).thenReturn(dto(10L, 1L));

        var out = service.getTaskById(10L);
        assertThat(out.id()).isEqualTo(10L);
    }

    @Test
    void getTaskById_notFound() {
        when(taskRepo.findById(77L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getTaskById(77L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Task 77 not found");
    }

    // ---------- PAGE ----------
    @Test
    void getPageOfTasks_ok() {
        var pageable = PageRequest.of(0, 2, Sort.by("id"));
        var e1 = entity(1L, 1L);
        var e2 = entity(2L, 1L);
        var page = new PageImpl<>(List.of(e1, e2), pageable, 4);

        when(taskRepo.findAll(pageable)).thenReturn(page);
        when(mapper.toResponse(e1)).thenReturn(dto(1L, 1L));
        when(mapper.toResponse(e2)).thenReturn(dto(2L, 1L));

        var out = service.getPageOfTasks(pageable);
        assertThat(out.getTotalTasks()).isEqualTo(4);
        assertThat(out.getTotalPages()).isEqualTo(2);
    }

    @Test
    void getPageOfTasks_outOfRange_throwsBadRequest() {
        var asked = PageRequest.of(5, 2, Sort.by("id"));
        var empty = new PageImpl<Task>(List.of(), asked, 6); // totalPages = 3
        when(taskRepo.findAll(asked)).thenReturn(empty);

        assertThatThrownBy(() -> service.getPageOfTasks(asked))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("This page does not exist");
    }

    // ---------- DELETE ----------
    @Test
    void deleteTask_ok_returnsSnapshot() {
        var e = entity(3L, 1L);
        when(taskRepo.findById(3L)).thenReturn(Optional.of(e));
        when(mapper.toResponse(e)).thenReturn(dto(3L, 1L));

        var out = service.deleteTaskById(3L);
        assertThat(out.id()).isEqualTo(3L);
        verify(taskRepo).delete(e);
    }

    @Test
    void deleteTask_notFound() {
        when(taskRepo.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deleteTaskById(404L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Task 404 not found");
    }
}
