package com.workpilot.service.DevisServices.DevisHistory;

import com.workpilot.dto.DevisDTO.DevisHistoryDTO;
import com.workpilot.entity.devis.Devis;
import com.workpilot.entity.devis.DevisHistory;
import com.workpilot.repository.devis.DevisHistoryRepository;
import com.workpilot.repository.devis.DevisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DevisHistoryServiceImpl implements DevisHistoryService {

    @Autowired
    private DevisHistoryRepository repository;
    @Autowired
    private DevisRepository devisRepository;

    @Override
    public List<DevisHistoryDTO> getHistoriesByDevisId(Long devisId) {
        return repository.findByDevisId(devisId).stream().map(history ->
                DevisHistoryDTO.builder()
                        .id(history.getId())
                        .version(history.getVersion())
                        .modificationDescription(history.getModificationDescription())
                        .action(history.getAction())
                        .date(history.getDate())
                        .name(history.getName())
                        .build()
        ).collect(Collectors.toList());
    }

    @Override
    public DevisHistoryDTO createHistory(Long devisId, DevisHistoryDTO dto) {
            Devis devis = devisRepository.findById(devisId)
                    .orElseThrow(() -> new RuntimeException("Devis not found"));
            DevisHistory h = DevisHistory.builder()
                    .version(dto.getVersion())
                    .modificationDescription(dto.getModificationDescription())
                    .action(dto.getAction())
                    .date(dto.getDate())
                    .name(dto.getName())
                    .devis(devis)
                    .build();
            return toDTO(repository.save(h));
        }
    @Override
    public DevisHistoryDTO updateHistory(Long id, DevisHistoryDTO dto) {
        DevisHistory history = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("History not found"));

        history.setVersion(dto.getVersion());
        history.setModificationDescription(dto.getModificationDescription());
        history.setDate(dto.getDate());
        history.setAction(dto.getAction());
        history.setName(dto.getName());

        return toDTO(repository.save(history));
    }

    @Override
    public void deleteHistory(Long id) {
        repository.deleteById(id);
    }

    private DevisHistoryDTO toDTO(DevisHistory h) {
        return DevisHistoryDTO.builder()
                .id(h.getId())
                .version(h.getVersion())
                .modificationDescription(h.getModificationDescription())
                .action(h.getAction())
                .date(h.getDate())
                .name(h.getName())
                .devisId(h.getDevis().getId())
                .build();
    }

    private DevisHistory toEntity(DevisHistoryDTO dto) {
        return DevisHistory.builder()
                .version(dto.getVersion())
                .modificationDescription(dto.getModificationDescription())
                .action(dto.getAction())
                .date(dto.getDate())
                .name(dto.getName())
                .build();
    }

    }


