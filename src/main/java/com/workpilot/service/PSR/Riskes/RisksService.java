package com.workpilot.service.PSR.Riskes;

import com.workpilot.dto.PsrDTO.RisksDTO;

import java.util.List;

public interface RisksService {

    RisksDTO addRiskToPsr(Long psrId, RisksDTO riskDTO);

    List<RisksDTO> getRisksByPsr(Long psrId);

    void deleteRisk(Long id);
}
