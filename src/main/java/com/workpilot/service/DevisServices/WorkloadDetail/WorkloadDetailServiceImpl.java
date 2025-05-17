package com.workpilot.service.DevisServices.WorkloadDetail;

import com.workpilot.dto.DevisDTO.WorkloadDetailDTO;
import com.workpilot.entity.devis.Devis;
import com.workpilot.entity.devis.WorkloadDetail;
import com.workpilot.entity.ressources.Demande;
import com.workpilot.repository.devis.DevisRepository;
import com.workpilot.repository.devis.WorkloadDetailRepository;
import com.workpilot.repository.ressources.DemandeRepository;
import com.workpilot.service.PublicHolidayService;
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

    @Autowired
    private PublicHolidayService publicHolidayService;

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

        if (updatedDetail.getPeriod() != null)
            existingDetail.setPeriod(updatedDetail.getPeriod());

        if (updatedDetail.getEstimatedWorkload() != null)
            existingDetail.setEstimatedWorkload(updatedDetail.getEstimatedWorkload());

        if (updatedDetail.getPublicHolidays() != null)
            existingDetail.setPublicHolidays(updatedDetail.getPublicHolidays());

        if (updatedDetail.getPublicHolidayDates() != null)
            existingDetail.setPublicHolidayDates(updatedDetail.getPublicHolidayDates());

        if (updatedDetail.getNumberOfResources() != null)
            existingDetail.setNumberOfResources(updatedDetail.getNumberOfResources());

        if (updatedDetail.getTotalEstimatedWorkload() != null)
            existingDetail.setTotalEstimatedWorkload(updatedDetail.getTotalEstimatedWorkload());

        if (updatedDetail.getNote() != null)
            existingDetail.setNote(updatedDetail.getNote());

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

        Demande demande = devis.getDemande();
        if (demande == null) {
            throw new RuntimeException("❌ Devis non lié à une demande valide.");
        }

        Map<String, WorkloadDetail> map = new LinkedHashMap<>();
        Set<LocalDate> holidays = publicHolidayService.getAllCombinedHolidaysBetween(
                demande.getDateDebut(), demande.getDateFin());

        LocalDate start = demande.getDateDebut();
        LocalDate end = demande.getDateFin();
        int realCount = demande.getTeamMembers() != null ? demande.getTeamMembers().size() : 0;
        int fakeCount = demande.getFakeMembers() != null ? demande.getFakeMembers().size() : 0;
        int memberCount = realCount + fakeCount;


        while (!start.isAfter(end)) {
            LocalDate monthStart = start.withDayOfMonth(1);
            LocalDate monthEnd = start.withDayOfMonth(start.lengthOfMonth());
            String key = start.getMonth().toString().substring(0, 3).toUpperCase() + " " + start.getYear();

            LocalDate effectiveStart = start.isBefore(demande.getDateDebut()) ? demande.getDateDebut() : start;
            LocalDate effectiveEnd = monthEnd.isAfter(demande.getDateFin()) ? demande.getDateFin() : monthEnd;

            int workingDays = calculateWorkingDays(effectiveStart, effectiveEnd, holidays);
            if (workingDays == 0) {
                start = start.withDayOfMonth(1).plusMonths(1);
                continue;
            }

            List<LocalDate> publicHolidaysInMonth = holidays.stream()
                    .filter(h -> !h.isBefore(effectiveStart) && !h.isAfter(effectiveEnd))
                    .filter(h -> h.getDayOfWeek() != DayOfWeek.SATURDAY && h.getDayOfWeek() != DayOfWeek.SUNDAY)
                    .collect(Collectors.toList());

            WorkloadDetail detail = new WorkloadDetail();
            detail.setPeriod(key);
            detail.setDevis(devis);
            detail.setDemande(demande);
            detail.setEstimatedWorkload(workingDays);
            detail.setPublicHolidays(publicHolidaysInMonth.size());
            detail.setPublicHolidayDates(publicHolidaysInMonth);
            detail.setNumberOfResources(memberCount);
            detail.setTotalEstimatedWorkload(workingDays * memberCount);
            detail.setTotalWorkload(0);
            detail.setNote("-");

            map.put(key, detail);
            start = start.withDayOfMonth(1).plusMonths(1);
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
                .publicHolidayDates(detail.getPublicHolidayDates()) // ✅ très important
                .totalEstimatedWorkload(detail.getTotalEstimatedWorkload())
                .numberOfResources(detail.getNumberOfResources())
                .devisId(detail.getDevis().getId())
                .totalWorkload(detail.getTotalWorkload())
                .note(detail.getNote())
                .build();
    }

}
