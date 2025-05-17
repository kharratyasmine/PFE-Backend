package com.workpilot.service.GestionRessources.PlannedWorkloadMember;

import com.workpilot.dto.GestionRessources.PlannedWorkloadMemberDTO;
import com.workpilot.entity.ressources.PlannedWorkloadMember;

import java.util.List;
import java.util.Optional;

public interface PlannedWorkloadMemberService {
    PlannedWorkloadMemberDTO save(PlannedWorkloadMemberDTO dto);
    PlannedWorkloadMemberDTO update(Long id, PlannedWorkloadMemberDTO dto);
    List<PlannedWorkloadMemberDTO> getByProject(Long projectId);
    List<PlannedWorkloadMemberDTO> getByProjectAndYear(Long projectId, int year);
    List<PlannedWorkloadMemberDTO> getByMember(Long memberId, Long projectId);
    void delete(Long id);
    void deleteWorkloadsByProjectAndMember(Long projectId, Long memberId);
    List<PlannedWorkloadMemberDTO> generateWorkloadsForProject(Long projectId);
    void generateForMember(Long projectId, Long memberId, double allocationInput);
}

