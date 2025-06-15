package com.workpilot.service.Dashboard.PlannedWorkload;

import com.workpilot.dto.Dashboard.PlannedWorkloadDTO;
import com.workpilot.entity.Dashboard.PlannedWorkload;
import com.workpilot.entity.ressources.Project;
import com.workpilot.repository.Dashboard.PlannedWorkloadRepository;
import com.workpilot.repository.ressources.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlannedWorkloadServiceImpl implements PlannedWorkloadService {

    private final PlannedWorkloadRepository repository;
    private final ProjectRepository projectRepository;

    @Override
    public List<PlannedWorkloadDTO> getByProjectId(Long projectId) {
        return repository.findByProjectId(projectId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PlannedWorkloadDTO save(PlannedWorkloadDTO dto) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        PlannedWorkload workload = toEntity(dto, project);
        return toDTO(repository.save(workload));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private PlannedWorkloadDTO toDTO(PlannedWorkload workload) {
        return PlannedWorkloadDTO.builder()
                .id(workload.getId())
                .month(workload.getMonth())
                .year(workload.getYear())
                .estimatedWorkloadPerResource(workload.getEstimatedWorkloadPerResource())
                .numberOfResources(workload.getNumberOfResources())
                .totalEstimatedWorkload(workload.getTotalEstimatedWorkload())
                .projectId(workload.getProject().getId())
                .projectName(workload.getProject().getName())
                .build();
    }

    private PlannedWorkload toEntity(PlannedWorkloadDTO dto, Project project) {
        return PlannedWorkload.builder()
                .id(dto.getId())
                .month(dto.getMonth())
                .year(dto.getYear())
                .estimatedWorkloadPerResource(dto.getEstimatedWorkloadPerResource())
                .numberOfResources(dto.getNumberOfResources())
                .totalEstimatedWorkload(dto.getTotalEstimatedWorkload())
                .project(project)
                .build();
    }
}
