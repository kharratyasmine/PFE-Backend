package com.workpilot.service.DevisServices.InvoicingDetail;

import com.workpilot.dto.DevisDTO.InvoicingDetailDTO;
import com.workpilot.entity.devis.Devis;
import com.workpilot.entity.devis.FinancialDetail;
import com.workpilot.entity.devis.InvoicingDetail;
import com.workpilot.entity.ressources.TeamMember;
import com.workpilot.repository.devis.DevisRepository;
import com.workpilot.repository.devis.InvoicingDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InvoicingDetailServiceImpl implements InvoicingDetailService {

    @Autowired
    private InvoicingDetailRepository invoicingDetailRepository;

    @Autowired
    private DevisRepository devisRepository;

    @Override
    public List<InvoicingDetail> GetAllInvoicingDetail() {
        return invoicingDetailRepository.findAll();
    }

    @Override
    public InvoicingDetail getInvoicingDetailById(Long id) {
        return invoicingDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("InvoicingDetail avec ID " + id + " introuvable."));
    }

    @Override
    public InvoicingDetail createInvoicingDetail(InvoicingDetail invoicingDetail) {
        return invoicingDetailRepository.save(invoicingDetail);
    }

    @Override
    public InvoicingDetail updateInvoicingDetail(Long id, InvoicingDetail updatedDetail) {
        InvoicingDetail existingDetail = invoicingDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("InvoicingDetail avec ID " + id + " introuvable."));

        if (updatedDetail.getDescription() != null) existingDetail.setDescription(updatedDetail.getDescription());
        if (updatedDetail.getInvoicingDate() != null) existingDetail.setInvoicingDate(updatedDetail.getInvoicingDate());
        if (updatedDetail.getAmount() != null) existingDetail.setAmount(updatedDetail.getAmount());
        System.out.println("✅ Mise à jour prête à être sauvegardée");

        return invoicingDetailRepository.save(existingDetail);
    }

    @Override
    public void deleteInvoicingDetail(Long id) {
        invoicingDetailRepository.deleteById(id);
    }

    @Override
    public List<InvoicingDetail> getByDevisId(Long devisId) {
        return invoicingDetailRepository.findByDevisId(devisId);
    }

    @Override
    public List<InvoicingDetailDTO> generateInvoicingDetails(Long devisId, int startMonth) {
        Devis devis = devisRepository.findById(devisId)
                .orElseThrow(() -> new RuntimeException("Devis not found"));

        int baseYear = (devis.getCreationDate() != null)
                ? devis.getCreationDate().getYear()
                : LocalDate.now().getYear();

        // Récupérer les membres du projet
        Set<TeamMember> members = devis.getProject().getTeams().stream()
                .flatMap(team -> team.getMembers().stream())
                .collect(Collectors.toSet());

        // Jours fériés fixes
        Set<LocalDate> publicHolidays = Set.of(
                LocalDate.of(baseYear, 1, 1),
                LocalDate.of(baseYear, 3, 20),
                LocalDate.of(baseYear, 4, 9),
                LocalDate.of(baseYear, 5, 1),
                LocalDate.of(baseYear, 7, 25),
                LocalDate.of(baseYear, 8, 13),
                LocalDate.of(baseYear, 10, 15),
                LocalDate.of(baseYear, 12, 17)
        );

        List<InvoicingDetail> details = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            YearMonth ym = YearMonth.of(baseYear, startMonth).plusMonths(i);
            int year = ym.getYear();
            int month = ym.getMonthValue();

            String description = getMonthName(month);
            int workloadDays = getTotalWorkloadWithDaysOff(members, year, month, publicHolidays);

            BigDecimal totalDailyCost = devis.getFinancialDetails().stream()
                    .map(FinancialDetail::getDailyCost)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal amount = totalDailyCost.multiply(BigDecimal.valueOf(workloadDays));
            LocalDate invoicingDate = ym.atEndOfMonth(); // ✅ Dernière date du mois

            details.add(new InvoicingDetail(null, description, invoicingDate, amount, devis));
        }

        return invoicingDetailRepository.saveAll(details)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    private int getTotalWorkloadWithDaysOff(Set<TeamMember> members, int year, int month, Set<LocalDate> publicHolidays) {
            return members.stream()
                    .mapToInt(member -> calculateWorkloadForMember(member, year, Month.of(month), publicHolidays))
                    .sum();
        }

        private int calculateWorkloadForMember(TeamMember member, int year, Month month, Set<LocalDate> publicHolidays) {
            int businessDays = 0;
            LocalDate date = LocalDate.of(year, month, 1);
            LocalDate end = date.withDayOfMonth(date.lengthOfMonth());

            Set<LocalDate> memberHolidays = member.getHoliday().stream()
                    .map(LocalDate::parse)
                    .collect(Collectors.toSet());

            while (!date.isAfter(end)) {
                DayOfWeek day = date.getDayOfWeek();
                boolean isWorkingDay = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
                boolean isNotPublicHoliday = !publicHolidays.contains(date);
                boolean isNotMemberHoliday = !memberHolidays.contains(date);

                if (isWorkingDay && isNotPublicHoliday && isNotMemberHoliday) {
                    businessDays++;
                }
                date = date.plusDays(1);
            }

            return businessDays;
        }

        private String getMonthName(int month) {
            return switch (month) {
                case 1 -> "janv";
                case 2 -> "fev";
                case 3 -> "mars";
                case 4 -> "avril";
                case 5 -> "mai";
                case 6 -> "juin";
                case 7 -> "juil";
                case 8 -> "août";
                case 9 -> "sept";
                case 10 -> "oct";
                case 11 -> "nov";
                case 12 -> "déc";
                default -> "inconnu";
            };
        }

        private InvoicingDetailDTO convertToDTO(InvoicingDetail detail) {
            return InvoicingDetailDTO.builder()
                    .id(detail.getId())
                    .description(detail.getDescription())
                    .invoicingDate(detail.getInvoicingDate())
                    .amount(detail.getAmount())
                    .devisId(detail.getDevis().getId())
                    .build();
        }
    }


