package com.moveo.ha.project;

import com.moveo.ha.dto.project.ProjectListDTO;
import com.moveo.ha.dto.project.ProjectRequestDTO;
import com.moveo.ha.dto.project.ProjectResponseDTO;
import com.moveo.ha.entity.Project;
import com.moveo.ha.error.BadRequestException;
import com.moveo.ha.error.NotFoundException;
import com.moveo.ha.mapper.ProjectMapper;
import com.moveo.ha.repository.ProjectRepository;
import com.moveo.ha.service.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectServiceImplTest {

    private ProjectRepository repository;
    private ProjectMapper mapper;
    private ProjectServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(ProjectRepository.class);
        mapper = mock(ProjectMapper.class);
        service = new ProjectServiceImpl(repository, mapper);
    }

    @Test
    void createProject_ok() {
        var req = new ProjectRequestDTO("N", "D");
        var entityToSave = Project.builder().name("N").description("D").build();
        var saved = Project.builder().id(1L).name("N").description("D")
                .createdAt(Instant.parse("2025-11-10T11:00:00Z"))
                .updatedAt(Instant.parse("2025-11-10T11:00:00Z"))
                .build();
        var dto = new ProjectResponseDTO(1L, "N", "D", saved.getCreatedAt(), saved.getUpdatedAt(), List.of());

        when(mapper.toEntity(req)).thenReturn(entityToSave);
        when(repository.save(entityToSave)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(dto);

        var out = service.createProject(req);

        assertThat(out).isEqualTo(dto);
        verify(mapper).toEntity(req);
        verify(repository).save(entityToSave);
        verify(mapper).toResponse(saved);
    }

    @Test
    void updateProject_ok() {
        var id = 5L;
        var req = new ProjectRequestDTO("New", "Desc");
        var existing = Project.builder().id(id).name("Old").description("OldD").build();
        var updated = Project.builder().id(id).name("New").description("Desc").build();
        var dto = new ProjectResponseDTO(id, "New", "Desc", null, null, List.of());

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(updated);
        when(mapper.toResponse(updated)).thenReturn(dto);

        var out = service.updateProjectById(id, req);

        assertThat(out).isEqualTo(dto);
        assertThat(existing.getName()).isEqualTo("New");
        assertThat(existing.getDescription()).isEqualTo("Desc");
        verify(repository).save(existing);
    }

    @Test
    void updateProject_notFound() {
        when(repository.findById(42L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateProjectById(42L, new ProjectRequestDTO("A", "B")))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Project 42 not found");
    }

    @Test
    void getProjectById_ok() {
        var p = Project.builder().id(2L).name("N").description("D").build();
        var dto = new ProjectResponseDTO(2L, "N", "D", null, null, List.of());

        when(repository.findById(2L)).thenReturn(Optional.of(p));
        when(mapper.toResponse(p)).thenReturn(dto);

        var out = service.getProjectById(2L);
        assertThat(out).isEqualTo(dto);
    }

    @Test
    void getProjectById_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getProjectById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Project 99 not found");
    }

    @Test
    void getPageOfProjects_ok_withSortMeta() {
        var pageable = PageRequest.of(0, 2, Sort.by(Sort.Order.asc("name")));

        var e1 = Project.builder().id(1L).name("A").description("D1").build();
        var e2 = Project.builder().id(2L).name("B").description("D2").build();
        var page = new PageImpl<>(List.of(e1, e2), pageable, 5);

        var d1 = new ProjectResponseDTO(1L, "A", "D1", null, null, List.of());
        var d2 = new ProjectResponseDTO(2L, "B", "D2", null, null, List.of());

        when(repository.findAll(pageable)).thenReturn(page);
        when(mapper.toResponse(e1)).thenReturn(d1);
        when(mapper.toResponse(e2)).thenReturn(d2);

        ProjectListDTO out = service.getPageOfProjects(pageable);

        assertThat(out.getTotalProjects()).isEqualTo(5);
        assertThat(out.getPageNumber()).isEqualTo(0);
        assertThat(out.getPageSize()).isEqualTo(2);
        assertThat(out.getTotalPages()).isEqualTo(3);
        assertThat(out.isFirst()).isTrue();
        assertThat(out.isLast()).isFalse();
        assertThat(out.getSortBy()).isEqualTo("name");
        assertThat(out.getSortDir()).isEqualTo("asc");
        assertThat(out.getProjects()).containsExactly(d1, d2);
    }

    @Test
    void getPageOfProjects_outOfRange_throwsBadRequest() {
        var asked = PageRequest.of(5, 2, Sort.by("id"));
        var emptyPage = new PageImpl<Project>(List.of(), asked, 6); // -> totalPages=3

        when(repository.findAll(asked)).thenReturn(emptyPage);

        assertThatThrownBy(() -> service.getPageOfProjects(asked))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("This page does not exist");
    }

    @Test
    void deleteProjectById_ok() {
        var id = 7L;
        var e = Project.builder().id(id).name("N").description("D").build();
        var dto = new ProjectResponseDTO(id, "N", "D", null, null, List.of());

        when(repository.findById(id)).thenReturn(Optional.of(e));
        when(mapper.toResponse(e)).thenReturn(dto);

        var out = service.deleteProjectById(id);

        assertThat(out).isEqualTo(dto);
        verify(repository).delete(e);
    }

    @Test
    void deleteProjectById_notFound() {
        when(repository.findById(77L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deleteProjectById(77L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Project 77 not found");
    }
}
