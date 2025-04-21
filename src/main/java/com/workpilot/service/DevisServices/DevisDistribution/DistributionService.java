package com.workpilot.service.DevisServices.DevisDistribution;

import com.workpilot.dto.DevisDTO.DistributionDTO;

import java.util.List;

public interface DistributionService {

    List<DistributionDTO> getDistributionsByDevisId(Long devisId);

    DistributionDTO addDistribution(Long devisId, DistributionDTO distributionDTO);

    DistributionDTO updateDistribution(Long distributionId, DistributionDTO distributionDTO);
}
