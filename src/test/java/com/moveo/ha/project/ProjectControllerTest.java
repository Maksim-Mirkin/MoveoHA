package com.moveo.ha.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moveo.ha.WebMvcTestSecurity;
import com.moveo.ha.controller.ProjectController;
import com.moveo.ha.dto.project.ProjectListDTO;
import com.moveo.ha.dto.project.ProjectRequestDTO;
import com.moveo.ha.dto.project.ProjectResponseDTO;
import com.moveo.ha.error.BadRequestException;
import com.moveo.ha.error.NotFoundException;
import com.moveo.ha.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = ProjectController.class)
@Import(WebMvcTestSecurity.class)
class ProjectControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    @MockitoBean
    ProjectService projectService;

    // ---------- CREATE ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProject_created201_andLocationHeader() throws Exception {
        var req = new ProjectRequestDTO("Website Redesign", "Marketing site redesign for Q4");
        var dto = new ProjectResponseDTO(1L, req.name(), req.description(), null, null, List.of());

        when(projectService.createProject(ArgumentMatchers.any())).thenReturn(dto);

        mvc.perform(post("/api/v1/projects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/projects/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProject_badRequest_whenValidationFails() throws Exception {
        var req = new ProjectRequestDTO("", "");

        mvc.perform(post("/api/v1/projects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/v1/projects"))
                .andExpect(jsonPath("$.*", hasSize(greaterThanOrEqualTo(5))));
    }

    @Test
    void createProject_unauthenticated_401() throws Exception {
        var req = new ProjectRequestDTO("N", "D");

        mvc.perform(post("/api/v1/projects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createProject_forbidden_403() throws Exception {
        var req = new ProjectRequestDTO("N", "D");

        mvc.perform(post("/api/v1/projects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // ---------- UPDATE (POST /{id}) ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProject_ok200() throws Exception {
        var req = new ProjectRequestDTO("Website Rebrand", "Scope updated");
        var dto = new ProjectResponseDTO(5L, req.name(), req.description(), null, null, List.of());
        when(projectService.updateProjectById(eq(5L), any())).thenReturn(dto);

        mvc.perform(post("/api/v1/projects/5")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProject_notFound404() throws Exception {
        when(projectService.updateProjectById(eq(999L), any()))
                .thenThrow(new NotFoundException("Project 999 not found"));

        mvc.perform(post("/api/v1/projects/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new ProjectRequestDTO("A", "B"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("Project 999 not found")));
    }

    @Test
    void updateProject_unauthenticated_401() throws Exception {
        var req = new ProjectRequestDTO("N", "D");

        mvc.perform(post("/api/v1/projects/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateProject_forbidden_403() throws Exception {
        var req = new ProjectRequestDTO("N", "D");

        mvc.perform(post("/api/v1/projects/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // ---------- GET BY ID ----------

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void getProjectById_ok200() throws Exception {
        var dto = new ProjectResponseDTO(10L, "N", "D", null, null, List.of());
        when(projectService.getProjectById(10L)).thenReturn(dto);

        mvc.perform(get("/api/v1/projects/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("N"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void getProjectById_notFound404() throws Exception {
        when(projectService.getProjectById(77L)).thenThrow(new NotFoundException("Project 77 not found"));

        mvc.perform(get("/api/v1/projects/77"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getProjectById_unauthenticated_401() throws Exception {
        mvc.perform(get("/api/v1/projects/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "GUEST")
    void getProjectById_forbidden_403() throws Exception {
        mvc.perform(get("/api/v1/projects/1"))
                .andExpect(status().isForbidden());
    }

    // ---------- GET PAGE ----------

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void getPage_ok200() throws Exception {
        var pageDto = ProjectListDTO.builder()
                .totalProjects(2)
                .pageNumber(0)
                .pageSize(20)
                .totalPages(1)
                .first(true)
                .last(true)
                .sortBy("id")
                .sortDir("asc")
                .projects(List.of(
                        new ProjectResponseDTO(1L, "A", "DA", null, null, List.of()),
                        new ProjectResponseDTO(2L, "B", "DB", null, null, List.of())
                ))
                .build();

        when(projectService.getPageOfProjects(any(org.springframework.data.domain.PageRequest.class))).thenReturn(pageDto);

        mvc.perform(get("/api/v1/projects")
                        .with(csrf())
                        .param("pageNumber", "0")
                        .param("pageSize", "20")
                        .param("sortBy", "id")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(2))
                .andExpect(jsonPath("$.projects", hasSize(2)));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void getPage_outOfRange_400() throws Exception {
        doThrow(new BadRequestException("This page does not exist."))
                .when(projectService).getPageOfProjects(any(org.springframework.data.domain.Pageable.class));

        mvc.perform(get("/api/v1/projects")
                        .param("pageNumber", "9999")
                        .param("pageSize", "20")
                        .param("sortBy", "id")
                        .param("sortDir", "asc"))
                .andExpect(status().isBadRequest());

        ArgumentCaptor<org.springframework.data.domain.Pageable> captor =
                ArgumentCaptor.forClass(org.springframework.data.domain.Pageable.class);
        verify(projectService).getPageOfProjects(captor.capture());
        var passed = captor.getValue();
        assertEquals(9999, passed.getPageNumber());
        assertEquals(20, passed.getPageSize());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void getPage_invalidSortField_400() throws Exception {
        mvc.perform(get("/api/v1/projects")
                        .with(csrf())
                        .param("pageNumber", "0")
                        .param("pageSize", "20")
                        .param("sortBy", "evilField")
                        .param("sortDir", "asc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPage_unauthenticated_401() throws Exception {
        mvc.perform(get("/api/v1/projects")
                        .param("pageNumber", "0")
                        .param("pageSize", "20")
                        .param("sortBy", "id")
                        .param("sortDir", "asc"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "GUEST")
        // lacks ADMIN/USER
    void getPage_forbidden_403() throws Exception {
        mvc.perform(get("/api/v1/projects")
                        .param("pageNumber", "0")
                        .param("pageSize", "20")
                        .param("sortBy", "id")
                        .param("sortDir", "asc"))
                .andExpect(status().isForbidden());
    }

    // ---------- DELETE ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_ok200_returnsSnapshot() throws Exception {
        var dto = new ProjectResponseDTO(3L, "X", "DX", null, null, List.of());
        when(projectService.deleteProjectById(3L)).thenReturn(dto);

        mvc.perform(delete("/api/v1/projects/3").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_notFound404() throws Exception {
        when(projectService.deleteProjectById(404L))
                .thenThrow(new NotFoundException("Project 404 not found"));

        mvc.perform(delete("/api/v1/projects/404").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void delete_unauthenticated_401() throws Exception {
        mvc.perform(delete("/api/v1/projects/1").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void delete_forbidden_403() throws Exception {
        mvc.perform(delete("/api/v1/projects/1").with(csrf()))
                .andExpect(status().isForbidden());
    }
}
