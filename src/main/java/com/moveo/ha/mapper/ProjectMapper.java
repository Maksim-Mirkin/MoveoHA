package com.moveo.ha.mapper;

import com.moveo.ha.dto.project.ProjectRequestDTO;
import com.moveo.ha.dto.project.ProjectResponseDTO;
import com.moveo.ha.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ProjectMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Project toEntity(ProjectRequestDTO dto);

    ProjectResponseDTO toResponse(Project entity);
}

