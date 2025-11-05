package cz.pavel.taskmanagement.backend.service;

import cz.pavel.taskmanagement.backend.dto.project.ProjectCreateDTO;
import cz.pavel.taskmanagement.backend.dto.project.ProjectResponseDTO;
import cz.pavel.taskmanagement.backend.dto.project.ProjectUpdateDTO;
import cz.pavel.taskmanagement.backend.entity.Project;
import cz.pavel.taskmanagement.backend.entity.User;
import cz.pavel.taskmanagement.backend.exception.ResourceNotFoundException;
import cz.pavel.taskmanagement.backend.repository.ProjectRepository;
import cz.pavel.taskmanagement.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public List<ProjectResponseDTO> getAllProjects() {
        log.info("Fetching all projects");
        return projectRepository.findAll().stream()
                .map(ProjectResponseDTO::new)
                .collect(Collectors.toList());
    }

    public ProjectResponseDTO getProjectById(Long id) {
        log.info("Fetching project with id: {}", id);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
        return new ProjectResponseDTO(project);
    }

    public List<ProjectResponseDTO> getProjectsByOwner(Long ownerId) {
        log.info("Fetching projects for owner id: {}", ownerId);
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", ownerId));

        return projectRepository.findByOwner(owner).stream()
                .map(ProjectResponseDTO::new)
                .collect(Collectors.toList());
    }

    public ProjectResponseDTO createProject(ProjectCreateDTO createDTO, Long ownerId) {
        log.info("Creating new project: {} for owner id: {}", createDTO.getName(), ownerId);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", ownerId));

        Project project = new Project();
        project.setName(createDTO.getName());
        project.setDescription(createDTO.getDescription());
        project.setOwner(owner);

        Project savedProject = projectRepository.save(project);
        log.info("Project created successfully with id: {}", savedProject.getId());

        return new ProjectResponseDTO(savedProject);
    }

    public ProjectResponseDTO updateProject(Long id, ProjectUpdateDTO updateDTO) {
        log.info("Updating project with id: {}", id);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));

        if (updateDTO.getName() != null) {
            project.setName(updateDTO.getName());
        }

        if (updateDTO.getDescription() != null) {
            project.setDescription(updateDTO.getDescription());
        }

        Project updatedProject = projectRepository.save(project);
        log.info("Project updated successfully with id: {}", updatedProject.getId());

        return new ProjectResponseDTO(updatedProject);
    }

    public void deleteProject(Long id) {
        log.info("Deleting project with id: {}", id);

        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project", id);
        }

        projectRepository.deleteById(id);
        log.info("Project deleted successfully with id: {}", id);
    }
}