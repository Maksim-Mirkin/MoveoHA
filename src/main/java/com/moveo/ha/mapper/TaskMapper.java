package com.moveo.ha.mapper;

import com.moveo.ha.dto.project.ProjectSummaryDTO;
import com.moveo.ha.dto.task.TaskRequestDTO;
import com.moveo.ha.dto.task.TaskResponseDTO;
import com.moveo.ha.entity.Project;
import com.moveo.ha.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskMapper {
    @Mapping(target = "project", expression = "java(toProjectSummary(entity.getProject()))")
    TaskResponseDTO toResponse(Task entity);

    ProjectSummaryDTO toProjectSummary(Project project);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "project", ignore = true)
    Task toEntity(TaskRequestDTO dto);
}

