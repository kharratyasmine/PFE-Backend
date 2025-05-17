package com.workpilot.service.PSR.Riskes;

import com.workpilot.dto.PsrDTO.RisksDTO;
import com.workpilot.entity.PSR.Psr;
import com.workpilot.entity.PSR.Risks;

import com.workpilot.repository.Psr.PsrRepository;
import com.workpilot.repository.Psr.RisksRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RisksServiceImpl implements RisksService {

    @Autowired
    private RisksRepository risksRepository;

    @Autowired
    private PsrRepository psrRepository;

    @Override
    public RisksDTO addRiskToPsr(Long psrId, RisksDTO riskDTO) {
        Psr psr = psrRepository.findById(psrId)
                .orElseThrow(() -> new EntityNotFoundException("PSR not found"));

        Risks risk = new Risks();
        risk.setDescription(riskDTO.getDescription());
        risk.setImpact(riskDTO.getImpact());
        risk.setProbability(riskDTO.getProbability());
        risk.setMitigationPlan(riskDTO.getMitigationPlan());
        risk.setPsr(psr);

        Risks saved = risksRepository.save(risk);

        RisksDTO result = new RisksDTO();
        result.setId(saved.getId());
        result.setDescription(saved.getDescription());
        result.setImpact(saved.getImpact());
        result.setProbability(saved.getProbability());
        result.setMitigationPlan(saved.getMitigationPlan());

        return result;
    }


    @Override
    public List<RisksDTO> getRisksByPsr(Long psrId) {
        return risksRepository.findByPsrId(psrId)
                .stream()
                .map(risk -> new RisksDTO(
                        risk.getId(),
                        risk.getDescription(),
                        risk.getProbability(),
                        risk.getImpact(),
                        risk.getMitigationPlan()
                )).collect(Collectors.toList());
    }

    @Override
    public void deleteRisk(Long id) {
        risksRepository.deleteById(id);
    }
}
