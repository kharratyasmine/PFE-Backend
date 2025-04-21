package com.workpilot.service.DevisServices.devis;

import com.workpilot.dto.DevisDTO.DevisDTO;


import java.util.List;
import java.util.Optional;

public interface DevisService {

  List<DevisDTO> getAllDevis();
  DevisDTO createDevis(DevisDTO devisDTO);
  DevisDTO updateDevis(Long id, DevisDTO devisDTO);
  Optional<DevisDTO> getDevisById(Long id);
  void deleteDevis(Long id);
  List<DevisDTO> getDevisByProjectId(Long projectId);

}
