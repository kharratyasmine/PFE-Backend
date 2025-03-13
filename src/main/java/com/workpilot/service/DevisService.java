package com.workpilot.service;

import com.workpilot.entity.Devis;
import java.util.List;

public interface DevisService {
  List<Devis> getAllDevis();
    Devis getDevisById(Long idDevis);
    Devis createDevis(Devis devis);
    Devis updateDevis(Long idDevis, Devis devis);
    void deleteDevis(Long idDevis);
}
