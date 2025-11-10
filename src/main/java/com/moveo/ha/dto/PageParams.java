package com.moveo.ha.dto;

import com.moveo.ha.error.BadRequestException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Objects;
import java.util.Set;

@Data
@Schema(description = "Common paging & sorting query parameters.")
public class PageParams {

    @Schema(description = "Zero-based page index", example = "0", defaultValue = "0")
    @Min(0)
    private Integer pageNumber = 0;

    @Schema(description = "Page size", example = "20", defaultValue = "20")
    @Min(1) @Max(200)
    private Integer pageSize = 20;

    @Schema(description = "Primary sort field", example = "id", defaultValue = "id")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "sortBy must be alphanumeric/underscore")
    private String sortBy = "id";

    @Schema(description = "Sort direction (asc|desc)", example = "asc", defaultValue = "asc")
    @Pattern(regexp = "^(?i)(asc|desc)$", message = "sortDir must be 'asc' or 'desc'")
    private String sortDir = "asc";

    /**
     * Convert to Pageable using a white-list of allowed sort fields.
     * If sortBy is not allowed → throws BadRequestException (HTTP 400).
     */
    public Pageable toPageable(Set<String> allowedSortBy) {
        Objects.requireNonNull(allowedSortBy, "allowedSortBy must not be null");

        if (!allowedSortBy.contains(sortBy)) {
            throw new BadRequestException(
                    "Invalid sortBy field '%s'. Allowed: %s".formatted(sortBy, allowedSortBy)
            );
        }

        var direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));
    }
}
