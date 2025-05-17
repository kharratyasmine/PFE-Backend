package com.workpilot.dto.GestionRessources;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TeamAllocationDTO {
    private Long teamId;
    private String teamName;
    private List<TeamMemberAllocationDTO> members;


    // getters et setters
}

