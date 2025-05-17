package com.workpilot.service.DevisServices.FinancialDetail;

import com.workpilot.entity.devis.Devis;
import com.workpilot.entity.devis.FinancialDetail;
import com.workpilot.entity.ressources.Demande;
import com.workpilot.entity.ressources.TeamMember;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Set;

public interface FinancialDetailService {
    List<FinancialDetail> GetAllFinancialDetail();

    FinancialDetail createFinancialDetail(FinancialDetail financialDetail);

    FinancialDetail updateFinancialDetail(Long id, FinancialDetail updatedDetail);

    FinancialDetail getFinancialDetailById(Long id);

    void deleteFinancialDetail(Long id);
    List<FinancialDetail> getByDevisId(Long devisId);
    List<FinancialDetail> generateFromTeamMembers(
            List<TeamMember> teamMembers,
            LocalDate startDate,
            LocalDate endDate,
            Devis devis,
            Demande demande)
;}
