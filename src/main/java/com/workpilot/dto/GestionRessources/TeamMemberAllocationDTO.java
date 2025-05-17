package com.workpilot.dto.GestionRessources;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TeamMemberAllocationDTO {
    private Long id;
    private Long memberId;
    private Double allocation;
    private Long projectId;
    private Long teamId;

    public TeamMemberAllocationDTO(Long id, Long memberId,Long projectId, Double allocation) {
        this.id = id;
        this.memberId = memberId;
        this.projectId=projectId;
        this.allocation = allocation;
    }

}

