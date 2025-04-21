package com.workpilot.dto;

import com.workpilot.entity.ressources.Demande;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientDTO {
    private Long id;
    private String Company;
    private List<String> salesManagers;
    private String contact;
    private String address;
    private String email;
    private List<ProjectDTO> projects;

}
