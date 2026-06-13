package com.TMP.tms.dto.projectdto;

import com.TMP.tms.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    /**
     * Converts a Project database entity into a ProjectResponse DTO.
     */
    public ProjectResponse toResponse(Project project) {
        if (project == null) {
            return null; // Safe fallback
        }
        
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getOwnerUserId(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}