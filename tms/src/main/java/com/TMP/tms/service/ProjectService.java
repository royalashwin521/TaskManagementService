package com.TMP.tms.service;

import com.TMP.tms.configuration.CurrentUserService;
import com.TMP.tms.dto.projectdto.ProjectMapper;
import com.TMP.tms.dto.projectdto.ProjectRequest;
import com.TMP.tms.dto.projectdto.ProjectResponse;
import com.TMP.tms.entity.Project;
import com.TMP.tms.exception.BusinessException;
import com.TMP.tms.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final CurrentUserService currentUserService;

    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        UUID ownerUserId = currentUserService.getCurrentUserId();
        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        project.setOwnerUserId(ownerUserId);

        Project savedProject = projectRepository.save(project);

        return projectMapper.toResponse(savedProject);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll()
                .stream()
                .map(projectMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID id) {
        Project project = getProjectEntityById(id);
        return projectMapper.toResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(UUID id, ProjectRequest request) {
        Project existingProject = getProjectEntityById(id);

        existingProject.setName(request.name());
        existingProject.setDescription(request.description());

        Project updatedProject = projectRepository.save(existingProject);
        return projectMapper.toResponse(updatedProject);
    }

    @Transactional
    public void deleteProject(UUID id) {
        if (!projectRepository.existsById(id)) {
            throw new BusinessException("Project not found with ID: " + id);
        }
        projectRepository.deleteById(id);
    }

    /**
     * Helper to fetch the raw entity.
     * Used internally by update/delete or if TaskService needs to verify a project.
     */
    protected Project getProjectEntityById(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Project not found with ID: " + id));
    }

}