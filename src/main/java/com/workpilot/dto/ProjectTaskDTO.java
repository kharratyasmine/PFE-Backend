package com.workpilot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.workpilot.entity.ressources.TaskStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTaskDTO {
    private Long id;
    private String name;
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateDebut;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateFin;

    private TaskStatus status;
    private String progress;
    private Long projectId;
    private String projectName;

    private List<TaskAssignmentDTO> assignments;
}