package com.moveo.ha.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moveo.ha.controller.ProjectController;
import com.moveo.ha.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
class ProjectControllerValidationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    @MockitoBean
    ProjectService projectService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void validationError_shapeMatchesCustomHandler() throws Exception {
        String invalid = """
                    { "name": "", "description": "" }
                """;

        mvc.perform(post("/api/v1/projects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // top-level fields of your custom error
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/v1/projects"))
                .andExpect(jsonPath("$.timestamp", not(emptyOrNullString())))
                .andExpect(jsonPath("$.errorCode").value("PROJECT_VALIDATION_FAILED"))
                // errors[]
                .andExpect(jsonPath("$.errors", hasSize(2)))
                .andExpect(jsonPath("$.errors[*].field", containsInAnyOrder("name", "description")))
                .andExpect(jsonPath("$.errors[?(@.field=='name')].message", hasItem("must not be blank")))
                .andExpect(jsonPath("$.errors[?(@.field=='description')].message", hasItem("must not be blank")));
    }
}
