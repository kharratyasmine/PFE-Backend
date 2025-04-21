package com.workpilot.service.GestionProject.PlannedWorkload;

import com.workpilot.dto.PlannedWorkloadDTO;

import com.workpilot.entity.ressources.PlannedWorkload;
import com.workpilot.entity.ressources.Project;
import com.workpilot.exception.ResourceNotFoundException;
import com.workpilot.repository.PlannedWorkloadRepository;
import com.workpilot.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlannedWorkloadServiceImpl implements PlannedWorkloadService{

    private final PlannedWorkloadRepository workloadRepo;
    private final ProjectRepository projectRepo;

    public List<PlannedWorkloadDTO> getByProject(Long projectId) {
        return workloadRepo.findByProjectId(projectId).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public PlannedWorkloadDTO save(PlannedWorkloadDTO dto) {
        // Vérifier que les valeurs ne sont pas négatives
        if (dto.getEstimatedWorkloadPerResource() < 0 || dto.getNumberOfResources() < 0) {
            throw new IllegalArgumentException("Les valeurs de charge ou de ressources ne peuvent pas être négatives.");
        }

        // Vérifier s'il existe déjà une planification pour ce projet, ce mois et cette année.
        List<PlannedWorkload> existing = workloadRepo.findByProjectId(dto.getProjectId())
                .stream()
                .filter(pw -> pw.getMonth().equals(dto.getMonth()) && pw.getYear() == dto.getYear())
                .collect(Collectors.toList());
        if (!existing.isEmpty()) {
            throw new RuntimeException("Une planification pour " + dto.getMonth() + " " + dto.getYear() + " existe déjà.");
        }

        Project project = projectRepo.findById(dto.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Déterminer la charge totale estimée :
        // Vous allez ajouter un booléen dans le DTO (manualTotal) pour indiquer si le total est saisi manuellement.
        double totalEstimatedWorkload;
        if (dto.isManualTotal()) { // Ce champ doit être ajouté dans PlannedWorkloadDTO
            totalEstimatedWorkload = dto.getTotalEstimatedWorkload();
        } else {
            totalEstimatedWorkload = dto.getEstimatedWorkloadPerResource() * dto.getNumberOfResources();
        }

        PlannedWorkload workload = PlannedWorkload.builder()
                .month(dto.getMonth())
                .year(dto.getYear())
                .estimatedWorkloadPerResource(dto.getEstimatedWorkloadPerResource())
                .numberOfResources(dto.getNumberOfResources())
                .totalEstimatedWorkload(totalEstimatedWorkload)
                .project(project)
                .build();

        return convertToDTO(workloadRepo.save(workload));
    }


    public void delete(Long id) {
        workloadRepo.deleteById(id);
    }

    @Override
    public PlannedWorkloadDTO update(Long id, PlannedWorkloadDTO dto) {
        if (dto.getEstimatedWorkloadPerResource() < 0 || dto.getNumberOfResources() < 0) {
            throw new IllegalArgumentException("Les valeurs de charge ou de ressources ne peuvent pas être négatives.");
        }

        PlannedWorkload existing = workloadRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Planification non trouvée"));

        existing.setMonth(dto.getMonth());
        existing.setYear(dto.getYear());
        existing.setEstimatedWorkloadPerResource(dto.getEstimatedWorkloadPerResource());
        existing.setNumberOfResources(dto.getNumberOfResources());

        // Utilisation directe de la valeur du DTO permettant une modification manuelle
        existing.setTotalEstimatedWorkload(dto.getTotalEstimatedWorkload());

        return convertToDTO(workloadRepo.save(existing));
    }



    private PlannedWorkloadDTO convertToDTO(PlannedWorkload pw) {
        return PlannedWorkloadDTO.builder()
                .id(pw.getId())
                .month(pw.getMonth())
                .year(pw.getYear())
                .estimatedWorkloadPerResource(pw.getEstimatedWorkloadPerResource())
                .numberOfResources(pw.getNumberOfResources())
                .totalEstimatedWorkload(pw.getTotalEstimatedWorkload())
                .projectId(pw.getProject().getId())
                .projectName(pw.getProject().getName())
                .build();
    }
}