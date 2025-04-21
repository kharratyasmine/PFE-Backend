package com.workpilot.service.DevisServices.FinancialDetail;

import com.workpilot.entity.devis.FinancialDetail;
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
        FinancialDetail existing = financialDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FinancialDetail avec ID " + id + " introuvable."));

        if (updatedDetail.getPosition() != null) existing.setPosition(updatedDetail.getPosition());
        if (updatedDetail.getWorkload() != null) existing.setWorkload(updatedDetail.getWorkload());
        if (updatedDetail.getDailyCost() != null) existing.setDailyCost(updatedDetail.getDailyCost());
        if (updatedDetail.getDailyCost() != null && updatedDetail.getWorkload() != null) {
            existing.setTotalCost(updatedDetail.getDailyCost().multiply(BigDecimal.valueOf(updatedDetail.getWorkload())));
        }

        return financialDetailRepository.save(existing);
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
    public List<FinancialDetail> generateFromTeamMembers(List<TeamMember> teamMembers, int year, Month startMonth, Set<LocalDate> publicHolidays) {
        Map<String, List<TeamMember>> groupedByRole = teamMembers.stream()
                .collect(Collectors.groupingBy(member -> member.getRole().name()));

        return groupedByRole.entrySet().stream().map(entry -> {
            String role = entry.getKey();
            List<TeamMember> members = entry.getValue();
            int totalWorkload = members.size() * calculateWorkload(year, startMonth, publicHolidays);
            BigDecimal dailyCost = BigDecimal.valueOf(members.get(0).getCost());
            BigDecimal totalCost = dailyCost.multiply(BigDecimal.valueOf(totalWorkload));

            return FinancialDetail.builder()
                    .position(role + " (" + members.size() + ")")
                    .workload(totalWorkload)
                    .dailyCost(dailyCost)
                    .totalCost(totalCost)
                    .build();
        }).collect(Collectors.toList());
    }

    private int calculateWorkload(int year, Month startMonth, Set<LocalDate> publicHolidays) {
        int businessDays = 0;
        LocalDate current = LocalDate.of(year, startMonth.getValue(), 1);
        LocalDate end = current.plusMonths(3).minusDays(1);

        while (!current.isAfter(end)) {
            boolean isBusinessDay = current.getDayOfWeek() != DayOfWeek.SATURDAY
                    && current.getDayOfWeek() != DayOfWeek.SUNDAY
                    && !publicHolidays.contains(current);
            if (isBusinessDay) businessDays++;
            current = current.plusDays(1);
        }
        return businessDays;
    }
}