package com.workpilot.service;

import com.workpilot.entity.*;
import com.workpilot.exception.DevisNotFoundException;
import com.workpilot.repository.*;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DevisServiceImpl implements DevisService {

   @Autowired
    private DevisRepository devisRepository;
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ClientRepository customerRepository;  // Utilisation correct

    @Override
    public List<Devis> getAllDevis() {
        return devisRepository.findAll(); // No need for casting here, findAll() returns List<Devis>
    }
    @Override
    public Devis getDevisById(Long idDevis) {
        return devisRepository.findById(idDevis)
                .orElseThrow(() -> new DevisNotFoundException("Devis with ID " + idDevis + " not found"));
        // Throws custom exception when Devis not found
    }


        public Devis createDevis(Devis devis) {
            // Vérifie si le projet existe en base
            if (devis.getProject() == null || !projectRepository.existsById(devis.getProject().getId())) {
                throw new RuntimeException("Projet avec ID " + (devis.getProject() != null ? devis.getProject().getId() : "null") + " introuvable.");
            }


            // Associer le devis aux détails financiers
            if (devis.getFinancialDetails() != null) {
                for (FinancialDetail detail : devis.getFinancialDetails()) {
                    detail.setDevis(devis);
                }
            }

            // Associer le devis aux charges de travail
            if (devis.getWorkloadDetails() != null) {
                for (WorkloadDetail workload : devis.getWorkloadDetails()) {
                    workload.setDevis(devis);
                }
            }

            // Associer le devis aux détails de facturation
            if (devis.getInvoicingDetails() != null) {
                for (InvoicingDetail invoice : devis.getInvoicingDetails()) {
                    invoice.setDevis(devis);
                }
            }

            return devisRepository.save(devis);
        }

    public Devis updateDevis(Long id, Devis updatedDevis) {
        // Vérifier si le devis existe en base
        Devis existingDevis = devisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Devis avec ID " + id + " introuvable."));

        // Vérifier si le projet fourni existe
        if (updatedDevis.getProject() != null) {
            if (updatedDevis.getProject().getId() == null ||
                    !projectRepository.existsById(updatedDevis.getProject().getId())) {
                throw new RuntimeException("Projet avec ID " + updatedDevis.getProject().getId() + " introuvable.");
            }
            existingDevis.setProject(updatedDevis.getProject());
        }

        // Mise à jour des champs simples
        if (updatedDevis.getReference() != null) existingDevis.setReference(updatedDevis.getReference());
        if (updatedDevis.getEdition() != null) existingDevis.setEdition(updatedDevis.getEdition());
        if (updatedDevis.getCreationDate() != null) existingDevis.setCreationDate(updatedDevis.getCreationDate());
        if (updatedDevis.getStatus() != null) existingDevis.setStatus(updatedDevis.getStatus());
        if (updatedDevis.getTotalCost() != null) existingDevis.setTotalCost(updatedDevis.getTotalCost());
        if (updatedDevis.getProposalValidity() != null) existingDevis.setProposalValidity(updatedDevis.getProposalValidity());

        // Mise à jour des détails financiers
        if (updatedDevis.getFinancialDetails() != null) {
            for (FinancialDetail detail : updatedDevis.getFinancialDetails()) {
                detail.setDevis(existingDevis);
            }
            existingDevis.setFinancialDetails(updatedDevis.getFinancialDetails());
        }

        // Mise à jour des charges de travail
        if (updatedDevis.getWorkloadDetails() != null) {
            for (WorkloadDetail workload : updatedDevis.getWorkloadDetails()) {
                workload.setDevis(existingDevis);
            }
            existingDevis.setWorkloadDetails(updatedDevis.getWorkloadDetails());
        }

        // Mise à jour des détails de facturation
        if (updatedDevis.getInvoicingDetails() != null) {
            for (InvoicingDetail invoice : updatedDevis.getInvoicingDetails()) {
                invoice.setDevis(existingDevis);
            }
            existingDevis.setInvoicingDetails(updatedDevis.getInvoicingDetails());
        }

        // Sauvegarde et retour de l'objet mis à jour
        return devisRepository.save(existingDevis);
    }




    @Override
    public void deleteDevis(Long idDevis) {
        devisRepository.deleteById(idDevis);
    }
}
