package com.workpilot.service.PSR.Riskes;

import com.workpilot.dto.PsrDTO.RisksDTO;
import com.workpilot.entity.PSR.Psr;
import com.workpilot.entity.PSR.Risks;
import com.workpilot.repository.Psr.PsrRepository;
import com.workpilot.repository.Psr.RisksRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RisksServiceImpl implements RisksService {

    @Autowired
    private RisksRepository risksRepository;

    @Autowired
    private PsrRepository psrRepository;

    @Override
    public RisksDTO addRiskToPsr(Long psrId, RisksDTO dto) {
        Psr psr = psrRepository.findById(psrId)
                .orElseThrow(() -> new EntityNotFoundException("PSR not found"));

        dto.setId(null); // évite ObjectOptimisticLockingFailureException
        Risks risk = convertToEntity(dto);

        risk.setPsr(psr);
        risk.setWeek(psr.getWeek());
        Risks saved = risksRepository.save(risk);
        return convertToDTO(saved);
    }

    @Override
    public List<RisksDTO> getRisksByPsr(Long psrId) {
        return risksRepository.findByPsrId(psrId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteRisk(Long id) {
        risksRepository.deleteById(id);
    }

    // 🔁 Convertisseur Entity → DTO
    private RisksDTO convertToDTO(Risks risk) {
        return RisksDTO.builder()
                .id(risk.getId())
                .description(risk.getDescription())
                .origin(risk.getOrigin())
                .category(risk.getCategory())
                .openDate(convertDateToString(risk.getOpenDate()))
                .dueDate(convertDateToString(risk.getDueDate()))
                .causes(risk.getCauses())
                .consequences(risk.getConsequences())
                .appliedMeasures(risk.getAppliedMeasures())
                .probability(risk.getProbability())
                .gravity(risk.getGravity())
                .criticality(risk.getCriticality())
                .measure(risk.getMeasure())
                .riskAssessment(risk.getRiskAssessment())
                .riskTreatmentDecision(risk.getRiskTreatmentDecision())
                .justification(risk.getJustification())
                .idAction(risk.getIdAction())
                .riskStat(risk.getRiskStat())
                .closeDate(convertDateToString(risk.getCloseDate()))
                .impact(risk.getImpact())
                .mitigationPlan(risk.getMitigationPlan())
                .psrId(risk.getPsr() != null ? risk.getPsr().getId() : null)
                .build();
    }

    // 🔁 Convertisseur DTO → Entity
    private Risks convertToEntity(RisksDTO dto) {
        return Risks.builder()
                .id(dto.getId())
                .description(dto.getDescription())
                .origin(dto.getOrigin())
                .category(dto.getCategory())
                .openDate(convertStringToDate(dto.getOpenDate()))
                .dueDate(convertStringToDate(dto.getDueDate()))
                .causes(dto.getCauses())
                .consequences(dto.getConsequences())
                .appliedMeasures(dto.getAppliedMeasures())
                .probability(dto.getProbability())
                .gravity(dto.getGravity())
                .criticality(dto.getCriticality())
                .measure(dto.getMeasure())
                .riskAssessment(dto.getRiskAssessment())
                .riskTreatmentDecision(dto.getRiskTreatmentDecision())
                .justification(dto.getJustification())
                .idAction(dto.getIdAction())
                .riskStat(dto.getRiskStat())
                .closeDate(convertStringToDate(dto.getCloseDate()))
                .impact(dto.getImpact())
                .mitigationPlan(dto.getMitigationPlan())
                .build();
    }

    // Utils pour les dates
    private String convertDateToString(LocalDate date) {
        return date != null ? date.toString() : null;
    }

    private LocalDate convertStringToDate(String date) {
        return (date != null && !date.isEmpty()) ? LocalDate.parse(date) : null;
    }

    @Override
    public RisksDTO updateRisk(Long psrId, RisksDTO riskDTO) {
        Risks existingRisk = risksRepository.findById(riskDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException("Risk not found with id: " + riskDTO.getId()));

        Psr psr = psrRepository.findById(psrId)
                .orElseThrow(() -> new EntityNotFoundException("PSR not found with id: " + psrId));

        // Mise à jour des champs
        existingRisk.setDescription(riskDTO.getDescription());
        existingRisk.setOrigin(riskDTO.getOrigin());
        existingRisk.setCategory(riskDTO.getCategory());
        existingRisk.setOpenDate(convertStringToDate(riskDTO.getOpenDate()));
        existingRisk.setDueDate(convertStringToDate(riskDTO.getDueDate()));
        existingRisk.setCauses(riskDTO.getCauses());
        existingRisk.setConsequences(riskDTO.getConsequences());
        existingRisk.setAppliedMeasures(riskDTO.getAppliedMeasures());
        existingRisk.setProbability(riskDTO.getProbability());
        existingRisk.setGravity(riskDTO.getGravity());
        existingRisk.setCriticality(riskDTO.getCriticality());
        existingRisk.setMeasure(riskDTO.getMeasure());
        existingRisk.setRiskAssessment(riskDTO.getRiskAssessment());
        existingRisk.setRiskTreatmentDecision(riskDTO.getRiskTreatmentDecision());
        existingRisk.setJustification(riskDTO.getJustification());
        existingRisk.setIdAction(riskDTO.getIdAction());
        existingRisk.setRiskStat(riskDTO.getRiskStat());
        existingRisk.setCloseDate(convertStringToDate(riskDTO.getCloseDate()));
        existingRisk.setImpact(riskDTO.getImpact());
        existingRisk.setMitigationPlan(riskDTO.getMitigationPlan());

        existingRisk.setPsr(psr); // mise à jour du lien PSR
        existingRisk.setWeek(psr.getWeek());
        Risks updated = risksRepository.save(existingRisk);
        return convertToDTO(updated);
    }

}
