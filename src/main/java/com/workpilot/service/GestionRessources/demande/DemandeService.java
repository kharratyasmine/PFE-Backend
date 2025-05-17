package com.workpilot.service.GestionRessources.demande;

import com.workpilot.dto.GestionRessources.DemandeDTO;

import java.util.List;

public interface DemandeService {
        List<DemandeDTO> getAlldemandes();
        DemandeDTO createDemande(DemandeDTO demandeDTO);
        List<DemandeDTO> getDemandesByProject(Long projectId);
        DemandeDTO updateDemande(Long id, DemandeDTO demandeDTO);
        void deleteDemande(Long id);

    }

