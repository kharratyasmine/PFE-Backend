package com.workpilot.service.DevisServices.DevisHistory;

import com.workpilot.dto.DevisDTO.DevisHistoryDTO;

import java.util.List;

public interface DevisHistoryService {
    List<DevisHistoryDTO> getHistoriesByDevisId(Long devisId);
    DevisHistoryDTO createHistory(Long devisId, DevisHistoryDTO dto);
    DevisHistoryDTO updateHistory(Long id, DevisHistoryDTO dto);
    void deleteHistory(Long id);
}
