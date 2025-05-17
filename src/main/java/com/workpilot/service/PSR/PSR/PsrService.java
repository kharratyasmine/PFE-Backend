package com.workpilot.service.PSR.PSR;

import com.workpilot.dto.PsrDTO.PsrDTO;
import com.workpilot.entity.PSR.Psr;
import com.workpilot.entity.devis.Devis;
import com.workpilot.entity.ressources.Demande;

import java.util.List;

public interface PsrService {

    PsrDTO createPsr(PsrDTO psrDTO);

    List<PsrDTO> getAllPsrs();

    PsrDTO getPsrById(Long id);

    void deletePsr(Long id);

    List<PsrDTO> getPsrsByProject(Long projectId);

    PsrDTO updatePsr(Long id, PsrDTO psrDTO);

}
