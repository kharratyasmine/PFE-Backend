package com.workpilot.service.DevisServices.visa;

import com.workpilot.dto.DevisDTO.VisaDTO;

import java.util.List;

public interface VisaService {
        List<VisaDTO> getVisasByDevisId(Long devisId);
        VisaDTO addVisa(Long devisId, VisaDTO visaDTO);
        VisaDTO updateVisa(Long id, VisaDTO visaDTO);
        void deleteVisa(Long id);
    }

