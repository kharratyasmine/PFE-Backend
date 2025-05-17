package com.workpilot.service.DevisServices.FinancialDetail;

import com.workpilot.entity.devis.Devis;
import com.workpilot.entity.devis.FinancialDetail;
import com.workpilot.entity.ressources.Demande;
import com.workpilot.entity.ressources.TeamMember;
import com.workpilot.repository.devis.FinancialDetailRepository;
import com.workpilot.service.PublicHolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FinancialDetailServiceImpl implements FinancialDetailService {

    @Autowired
    private FinancialDetailRepository financialDetailRepository;

    @Autowired
    private PublicHolidayService publicHolidayService;


    @Override
    public List<FinancialDetail> GetAllFinancialDetail() {
        return financialDetailRepository.findAll();
    }

    @Override
    public FinancialDetail createFinancialDetail(FinancialDetail financialDetail) {
        return financialDetailRepository.save(financialDetail);
    }

    @Override
    public FinancialDetail updateFinancialDetail(Long id, FinancialDetail updatedDetail) {
        FinancialDetail existingDetail = financialDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FinancialDetail avec ID " + id + " introuvable."));

        if (updatedDetail.getPosition() != null) existingDetail.setPosition(updatedDetail.getPosition());
        if (updatedDetail.getWorkload() != null) existingDetail.setWorkload(updatedDetail.getWorkload());
        if (updatedDetail.getDailyCost() != null) existingDetail.setDailyCost(updatedDetail.getDailyCost());
        if (updatedDetail.getTotalCost() != null) existingDetail.setTotalCost(updatedDetail.getTotalCost());
        if (updatedDetail.getDailyCost() != null && updatedDetail.getWorkload() != null) {
            existingDetail.setTotalCost(updatedDetail.getDailyCost().multiply(BigDecimal.valueOf(updatedDetail.getWorkload())));
        }

        return financialDetailRepository.save(existingDetail);
    }

    @Override
    public FinancialDetail getFinancialDetailById(Long id) {
        return financialDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FinancialDetail avec ID " + id + " introuvable."));
    }

    @Override
    public void deleteFinancialDetail(Long id) {
        financialDetailRepository.deleteById(id);
    }

    @Override
    public List<FinancialDetail> getByDevisId(Long devisId) {
        return financialDetailRepository.findByDevisId(devisId);
    }


    @Override
    public List<FinancialDetail> generateFromTeamMembers(
            List<TeamMember> teamMembers,
            LocalDate startDate,
            LocalDate endDate,
            Devis devis,
            Demande demande) {

        // ✅ Appel au service pour obtenir les jours fériés dynamiques
        Set<LocalDate> publicHolidays = publicHolidayService.getAllCombinedHolidaysBetween(startDate, endDate);

        Map<String, List<TeamMember>> groupedByRole = teamMembers.stream()
                .collect(Collectors.groupingBy(member -> member.getRole().name()));

        List<FinancialDetail> financialDetails = new ArrayList<>();

        for (Map.Entry<String, List<TeamMember>> entry : groupedByRole.entrySet()) {
            String role = entry.getKey();
            List<TeamMember> members = entry.getValue();

            int totalWorkload = members.size() * calculateWorkload(startDate, endDate, publicHolidays);

            double avgCost = members.stream().mapToDouble(TeamMember::getCost).average().orElse(0.0);
            BigDecimal dailyCost = BigDecimal.valueOf(avgCost);
            BigDecimal totalCost = dailyCost.multiply(BigDecimal.valueOf(totalWorkload));

            FinancialDetail detail = FinancialDetail.builder()
                    .position(role + " (" + members.size() + ")")
                    .workload(totalWorkload)
                    .dailyCost(dailyCost)
                    .totalCost(totalCost)
                    .devis(devis)
                    .demande(demande)
                    .build();

            financialDetails.add(detail);
        }

        return financialDetailRepository.saveAll(financialDetails);
    }


    private int calculateWorkload(LocalDate start, LocalDate end, Set<LocalDate> publicHolidays) {
        int businessDays = 0;
        while (!start.isAfter(end)) {
            DayOfWeek day = start.getDayOfWeek();
            boolean isWorkingDay = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
            boolean isNotHoliday = !publicHolidays.contains(start);

            if (isWorkingDay && isNotHoliday) {
                businessDays++;
            }

            start = start.plusDays(1);
        }
        return businessDays;
    }
}
