package com.moveo.ha.service;

import com.moveo.ha.dto.project.ProjectListDTO;
import com.moveo.ha.dto.project.ProjectRequestDTO;
import com.moveo.ha.dto.project.ProjectResponseDTO;
import com.moveo.ha.error.BadRequestException;
import com.moveo.ha.error.NotFoundException;
import com.moveo.ha.mapper.ProjectMapper;
import com.moveo.ha.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@inheritDoc}
 */
@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    /** {@inheritDoc} */
    @Override
    public ProjectResponseDTO createProject(ProjectRequestDTO request) {
        log.info("Creating project name='{}'", request.name());
        var saved = projectRepository.save(projectMapper.toEntity(request));
        var dto = projectMapper.toResponse(saved);
        log.info("Created project id={}, name='{}'", dto.id(), dto.name());
        return dto;
    }

    /** {@inheritDoc} */
    @Override
    public ProjectResponseDTO updateProjectById(Long id, ProjectRequestDTO request) {
        log.info("Updating project id={} ...", id);
        var p = projectRepository.findById(id).orElseThrow(() -> {
            log.warn("Update failed: project id={} not found", id);
            return new NotFoundException("Project %d not found".formatted(id));
        });

        p.setName(request.name());
        p.setDescription(request.description());

        var updated = projectRepository.save(p);
        var dto = projectMapper.toResponse(updated);
        log.info("Updated project id={} -> name='{}'", id, dto.name());
        return dto;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public ProjectResponseDTO getProjectById(Long id) {
        log.debug("Fetching project id={}", id);
        return projectRepository.findById(id)
                .map(entity -> {
                    var dto = projectMapper.toResponse(entity);
                    log.debug("Fetched project id={} (name='{}')", id, dto.name());
                    return dto;
                })
                .orElseThrow(() -> {
                    log.warn("Get failed: project id={} not found", id);
                    return new NotFoundException("Project %d not found".formatted(id));
                });
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public ProjectListDTO getPageOfProjects(Pageable pageable) {
        log.debug("Listing projects page={} size={} sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        var page = projectRepository.findAll(pageable).map(projectMapper::toResponse);

        if (pageable.getPageNumber() >= page.getTotalPages() && page.getTotalPages() > 0) {
            throw new BadRequestException("This page does not exist.");

        }

        var sort = pageable.getSort();
        String sortBy = "id";
        String sortDir = "asc";
        var it = sort.iterator();
        if (it.hasNext()) {
            var order = it.next();
            sortBy = order.getProperty();
            sortDir = order.getDirection().name().toLowerCase();
        }

        var dto = ProjectListDTO.builder()
                .totalProjects(page.getTotalElements())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .sortBy(sortBy)
                .sortDir(sortDir)
                .projects(page.getContent())
                .build();

        log.debug("Listed projects: totalElements={} totalPages={}",
                page.getTotalElements(), page.getTotalPages());
        return dto;
    }

    /** {@inheritDoc} */
    @Override
    public ProjectResponseDTO deleteProjectById(Long id) {
        log.info("Deleting project id={} ...", id);
        var p = projectRepository.findById(id).orElseThrow(() -> {
            log.warn("Delete failed: project id={} not found", id);
            return new NotFoundException("Project %d not found".formatted(id));
        });

        var dto = projectMapper.toResponse(p);
        projectRepository.delete(p);
        log.info("Deleted project id={} (name='{}')", id, dto.name());
        return dto;
    }
}
