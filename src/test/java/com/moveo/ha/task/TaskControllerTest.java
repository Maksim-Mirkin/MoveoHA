package com.moveo.ha.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moveo.ha.WebMvcTestSecurity;
import com.moveo.ha.controller.TaskController;
import com.moveo.ha.dto.project.ProjectSummaryDTO;
import com.moveo.ha.dto.task.TaskListDTO;
import com.moveo.ha.dto.task.TaskRequestDTO;
import com.moveo.ha.dto.task.TaskResponseDTO;
import com.moveo.ha.enums.TaskStatus;
import com.moveo.ha.error.MoveoHAExceptionHandler;
import com.moveo.ha.error.NotFoundException;
import com.moveo.ha.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TaskController.class)
@Import({WebMvcTestSecurity.class, MoveoHAExceptionHandler.class})
public class TaskControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean TaskService taskService;

    private TaskRequestDTO validReq() {
        return new TaskRequestDTO(1L, "Prepare sprint demo", "Slides + video", TaskStatus.TODO);
    }

    private TaskResponseDTO sampleDto(long id) {
        return new TaskResponseDTO(
                id,
                "Prepare sprint demo",
                "Slides + video",
                TaskStatus.IN_PROGRESS,
                Instant.parse("2025-11-10T15:00:00Z"),
                Instant.parse("2025-11-10T15:12:00Z"),
                new ProjectSummaryDTO(1L, "Website Redesign", Instant.parse("2025-11-10T15:12:00Z"))
        );
    }

    // ---------- CREATE ----------

    @Test
    @WithMockUser(roles = {"ADMIN","USER"})
    void createTask_created201() throws Exception {
        when(taskService.createTask(any())).thenReturn(sampleDto(100L));

        mvc.perform(post("/api/v1/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(validReq())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    @WithMockUser(roles = {"ADMIN","USER"})
    void createTask_validation400_blankFields() throws Exception {
        var bad = new TaskRequestDTO(1L, "", "", TaskStatus.TODO);

        mvc.perform(post("/api/v1/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN","USER"})
    void createTask_invalidEnum400() throws Exception {
        var rawJson = """
          {"projectId":1,"title":"t","description":"d","status":"TODOasdasd"}
        """;

        mvc.perform(post("/api/v1/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTask_unauthenticated401() throws Exception {
        mvc.perform(post("/api/v1/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(validReq())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "GUEST")
    void createTask_forbidden403() throws Exception {
        mvc.perform(post("/api/v1/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(validReq())))
                .andExpect(status().isForbidden());
    }

    // ---------- UPDATE ----------

    @Test
    @WithMockUser(roles = {"ADMIN","USER"})
    void updateTask_ok200() throws Exception {
        when(taskService.updateTaskById(eq(5L), any())).thenReturn(sampleDto(5L));

        mvc.perform(post("/api/v1/tasks/5")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(validReq())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @WithMockUser(roles = {"ADMIN","USER"})
    void updateTask_notFound404() throws Exception {
        when(taskService.updateTaskById(eq(999L), any()))
                .thenThrow(new NotFoundException("Task 999 not found"));

        mvc.perform(post("/api/v1/tasks/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(validReq())))
                .andExpect(status().isNotFound());
    }

    // ---------- GET BY ID ----------

    @Test
    @WithMockUser(roles = {"ADMIN","USER"})
    void getTaskById_ok200() throws Exception {
        when(taskService.getTaskById(10L)).thenReturn(sampleDto(10L));

        mvc.perform(get("/api/v1/tasks/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.project.id").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN","USER"})
    void getTaskById_notFound404() throws Exception {
        when(taskService.getTaskById(77L)).thenThrow(new NotFoundException("Task 77 not found"));

        mvc.perform(get("/api/v1/tasks/77"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTaskById_unauthenticated401() throws Exception {
        mvc.perform(get("/api/v1/tasks/1"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- GET PAGE ----------

    @Test
    @WithMockUser(roles = {"ADMIN","USER"})
    void getPage_ok200() throws Exception {
        var list = TaskListDTO.builder()
                .totalTasks(2)
                .pageNumber(0)
                .pageSize(20)
                .totalPages(1)
                .first(true)
                .last(true)
                .sortBy("id")
                .sortDir("asc")
                .tasks(List.of(sampleDto(1L), sampleDto(2L)))
                .build();

        when(taskService.getPageOfTasks(any())).thenReturn(list);

        mvc.perform(get("/api/v1/tasks")
                        .param("pageNumber", "0")
                        .param("pageSize", "20")
                        .param("sortBy", "id")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").value(2))
                .andExpect(jsonPath("$.tasks", hasSize(2)));
    }

    @Test
    void getPage_unauthenticated401() throws Exception {
        mvc.perform(get("/api/v1/tasks")
                        .param("pageNumber", "0")
                        .param("pageSize", "20")
                        .param("sortBy", "id")
                        .param("sortDir", "asc"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- DELETE ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_ok200() throws Exception {
        when(taskService.deleteTaskById(3L)).thenReturn(sampleDto(3L));

        mvc.perform(delete("/api/v1/tasks/3").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_notFound404() throws Exception {
        when(taskService.deleteTaskById(404L))
                .thenThrow(new NotFoundException("Task 404 not found"));

        mvc.perform(delete("/api/v1/tasks/404").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_unauthenticated401() throws Exception {
        mvc.perform(delete("/api/v1/tasks/1").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void delete_forbidden403() throws Exception {
        mvc.perform(delete("/api/v1/tasks/1").with(csrf()))
                .andExpect(status().isForbidden());
    }
}
