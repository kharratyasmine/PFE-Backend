package com.workpilot.service.DevisServices.WorkloadDetail;

import com.workpilot.dto.DevisDTO.WorkloadDetailDTO;
import com.workpilot.entity.devis.Devis;
import com.workpilot.entity.devis.WorkloadDetail;
import com.workpilot.entity.ressources.Demande;
import com.workpilot.repository.devis.DevisRepository;
import com.workpilot.repository.devis.WorkloadDetailRepository;
import com.workpilot.repository.DemandeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkloadDetailServiceImpl implements WorkloadDetailService {

    @Autowired
    private WorkloadDetailRepository workloadDetailRepository;

    @Autowired
    private DevisRepository devisRepository;

    @Autowired
    private DemandeRepository demandeRepository;

    @Override
    public WorkloadDetail createWorkloadDetail(WorkloadDetail workloadDetail) {
        return workloadDetailRepository.save(workloadDetail);
    }

    @Override
    public List<WorkloadDetail> saveAll(List<WorkloadDetail> details) {
        return workloadDetailRepository.saveAll(details);
    }

    @Override
    public WorkloadDetail updateWorkloadDetail(Long id, WorkloadDetail updatedDetail) {
        WorkloadDetail existingDetail = workloadDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("WorkloadDetail avec ID " + id + " introuvable."));

        if (updatedDetail.getPeriod() != null) existingDetail.setPeriod(updatedDetail.getPeriod());
        if (updatedDetail.getEstimatedWorkload() != null) existingDetail.setEstimatedWorkload(updatedDetail.getEstimatedWorkload());
        if (updatedDetail.getPublicHolidays() != null) existingDetail.setPublicHolidays(updatedDetail.getPublicHolidays());


        System.out.println("✅ Mise à jour prête à être sauvegardée");

        return workloadDetailRepository.save(existingDetail);
    }


    @Override
    public WorkloadDetail getWorkloadDetailById(Long id) {
        return workloadDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("WorkloadDetail avec ID " + id + " introuvable."));
    }

    @Override
    public void deleteWorkloadDetail(Long id) {
        workloadDetailRepository.deleteById(id);
    }

    @Override
    public List<WorkloadDetail> GetAllWorkloadDetail() {
        return workloadDetailRepository.findAll();
    }

    @Override
    public List<WorkloadDetail> getByDevisId(Long devisId) {
        return workloadDetailRepository.findByDevisId(devisId);
    }

    @Override
    public List<WorkloadDetailDTO> generateFromDemandes(Long devisId) {
        Devis devis = devisRepository.findById(devisId)
                .orElseThrow(() -> new RuntimeException("❌ Devis not found for ID: " + devisId));

        List<Demande> demandes = demandeRepository.findByProjectId(devis.getProject().getId());

        Map<String, WorkloadDetail> map = new LinkedHashMap<>();

        // ✅ Définir les jours fériés
        int currentYear = LocalDate.now().getYear();
        Set<LocalDate> holidays = Set.of(
                LocalDate.of(currentYear, 1, 1),
                LocalDate.of(currentYear, 3, 20),
                LocalDate.of(currentYear, 4, 9),
                LocalDate.of(currentYear, 5, 1),
                LocalDate.of(currentYear, 7, 25),
                LocalDate.of(currentYear, 8, 13),
                LocalDate.of(currentYear, 10, 15),
                LocalDate.of(currentYear, 12, 17)
        );

        for (Demande demande : demandes) {
            LocalDate start = demande.getDateDebut();
            LocalDate end = demande.getDateFin();
            int memberCount = demande.getTeamMembers().size(); // ✅ nombre de ressources

            while (!start.isAfter(end)) {
                LocalDate monthStart = start.withDayOfMonth(1);
                LocalDate monthEnd = start.withDayOfMonth(start.lengthOfMonth());

                String key = start.getMonth().toString().substring(0, 3).toUpperCase() + " " + start.getYear();

                map.putIfAbsent(key, new WorkloadDetail());
                WorkloadDetail detail = map.get(key);
                detail.setPeriod(key);
                detail.setDevis(devis);

                // ✅ Appliquer limites réelles de la demande
                LocalDate effectiveStart = start.isBefore(demande.getDateDebut()) ? demande.getDateDebut() : start;
                LocalDate effectiveEnd = monthEnd.isAfter(demande.getDateFin()) ? demande.getDateFin() : monthEnd;

                int workingDays = calculateWorkingDays(effectiveStart, effectiveEnd, holidays);

                int publicHolidayCount = (int) holidays.stream()
                        .filter(h -> !h.isBefore(effectiveStart) && !h.isAfter(effectiveEnd))
                        .count();

                detail.setEstimatedWorkload(
                        detail.getEstimatedWorkload() == null ? workingDays : detail.getEstimatedWorkload() + workingDays
                );
                detail.setPublicHolidays(
                        detail.getPublicHolidays() == null ? publicHolidayCount : detail.getPublicHolidays() + publicHolidayCount
                );

                start = start.withDayOfMonth(1).plusMonths(1);
            }

        }

        return workloadDetailRepository.saveAll(map.values())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    private int calculateWorkingDays(LocalDate start, LocalDate end, Set<LocalDate> holidays) {
        int workingDays = 0;
        while (!start.isAfter(end)) {
            if (start.getDayOfWeek() != DayOfWeek.SATURDAY &&
                    start.getDayOfWeek() != DayOfWeek.SUNDAY &&
                    !holidays.contains(start)) {
                workingDays++;
            }
            start = start.plusDays(1);
        }
        return workingDays;
    }


    private WorkloadDetailDTO toDTO(WorkloadDetail detail) {
        return WorkloadDetailDTO.builder()
                .id(detail.getId())
                .period(detail.getPeriod())
                .estimatedWorkload(detail.getEstimatedWorkload())
                .publicHolidays(detail.getPublicHolidays())
                .devisId(detail.getDevis().getId())
                .build();
    }
}
