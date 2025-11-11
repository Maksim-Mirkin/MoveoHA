package com.moveo.ha.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moveo.ha.controller.TaskController;
import com.moveo.ha.service.TaskService;
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

@WebMvcTest(TaskController.class)
public class TaskControllerValidationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean TaskService taskService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void validationError_shapeMatchesCustomHandler() throws Exception {
        String invalid = """
            { "projectId": null, "title": "", "description": "", "status": null }
        """;

        mvc.perform(post("/api/v1/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/v1/tasks"))
                .andExpect(jsonPath("$.timestamp", not(emptyOrNullString())))
                .andExpect(jsonPath("$.errorCode").value("PROJECT_VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors", hasSize(4)))
                .andExpect(jsonPath("$.errors[*].field",
                        containsInAnyOrder("projectId", "title", "description", "status")))
                .andExpect(jsonPath("$.errors[?(@.field=='projectId')].message", hasItem("must not be null")))
                .andExpect(jsonPath("$.errors[?(@.field=='title')].message", hasItem("must not be blank")))
                .andExpect(jsonPath("$.errors[?(@.field=='description')].message", hasItem("must not be blank")))
                .andExpect(jsonPath("$.errors[?(@.field=='status')].message", hasItem("must not be null")));
    }
}

