package com.workpilot.service.DevisServices.InvoicingDetail;

import com.workpilot.dto.DevisDTO.InvoicingDetailDTO;
import com.workpilot.entity.devis.Devis;
import com.workpilot.entity.devis.FinancialDetail;
import com.workpilot.entity.devis.InvoicingDetail;
import com.workpilot.entity.ressources.Demande;
import com.workpilot.repository.devis.DevisRepository;
import com.workpilot.repository.devis.FinancialDetailRepository;
import com.workpilot.repository.devis.InvoicingDetailRepository;
import com.workpilot.service.PublicHolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class InvoicingDetailServiceImpl implements InvoicingDetailService {

    @Autowired
    private InvoicingDetailRepository invoicingDetailRepository;

    @Autowired
    private DevisRepository devisRepository;

    @Autowired
    private FinancialDetailRepository financialDetailRepository;

    @Autowired
    private PublicHolidayService publicHolidayService;

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
    public List<InvoicingDetailDTO> generateInvoicingDetails(Long devisId) {
        Devis devis = devisRepository.findById(devisId)
                .orElseThrow(() -> new RuntimeException("\ud83d\udce6 Devis avec ID " + devisId + " introuvable"));

        Demande demande = devis.getDemande();
        if (demande == null) {
            throw new RuntimeException("\u274c Devis non li\u00e9 \u00e0 une demande.");
        }

        LocalDate start = demande.getDateDebut();
        LocalDate end = demande.getDateFin();

        if (start == null || end == null || end.isBefore(start)) {
            throw new RuntimeException("\u274c Dates invalides dans la demande (start: " + start + ", end: " + end + ")");
        }

        List<FinancialDetail> financialDetails = financialDetailRepository.findByDevisId(devisId);
        if (financialDetails.isEmpty()) {
            throw new RuntimeException("\u274c Aucun d\u00e9tail financier trouv\u00e9 pour ce devis.");
        }

        Set<LocalDate> publicHolidays = publicHolidayService.getAllCombinedHolidaysBetween(start, end);

        List<YearMonth> months = new ArrayList<>();
        YearMonth current = YearMonth.from(start);
        YearMonth endMonth = YearMonth.from(end);

        while (!current.isAfter(endMonth)) {
            months.add(current);
            current = current.plusMonths(1);
        }

        BigDecimal totalCost = financialDetails.stream()
                .map(FinancialDetail::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthlyAmount = totalCost.divide(BigDecimal.valueOf(months.size()), 2, RoundingMode.HALF_UP);

        List<InvoicingDetailDTO> result = new ArrayList<>();
        for (YearMonth ym : months) {
            LocalDate lastDay = ym.atEndOfMonth();
            result.add(InvoicingDetailDTO.builder()
                    .description(getMonthName(ym.getMonthValue()) + " " + ym.getYear())
                    .invoicingDate(lastDay)
                    .amount(monthlyAmount)
                    .build());
        }

        return result;
    }

    private String getMonthName(int month) {
        return switch (month) {
            case 1 -> "janv";
            case 2 -> "févr";
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
