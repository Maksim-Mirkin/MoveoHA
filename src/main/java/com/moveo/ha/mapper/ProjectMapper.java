package com.moveo.ha.mapper;

import com.moveo.ha.dto.project.ProjectRequestDTO;
import com.moveo.ha.dto.project.ProjectResponseDTO;
import com.moveo.ha.dto.task.TaskSummaryDTO;
import com.moveo.ha.entity.Project;
import com.moveo.ha.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Project toEntity(ProjectRequestDTO dto);

    @Mapping(target = "tasks", source = "tasks")
    ProjectResponseDTO toResponse(Project entity);

    TaskSummaryDTO toTaskSummary(Task task);
}


