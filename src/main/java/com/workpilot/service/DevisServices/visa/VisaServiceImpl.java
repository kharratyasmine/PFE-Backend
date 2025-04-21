package com.workpilot.service.DevisServices.visa;

import com.workpilot.dto.DevisDTO.VisaDTO;
import com.workpilot.entity.devis.Devis;
import com.workpilot.entity.devis.Visa;
import com.workpilot.repository.devis.DevisRepository;
import com.workpilot.repository.devis.VisaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VisaServiceImpl implements VisaService {

    @Autowired
    private VisaRepository visaRepository;

    @Autowired
    private DevisRepository devisRepository;

    @Override
    public List<VisaDTO> getVisasByDevisId(Long devisId) {
        return visaRepository.findByDevisId(devisId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public VisaDTO addVisa(Long devisId, VisaDTO dto) {
        if (visaRepository.existsByActionAndDevisId(dto.getAction(), devisId)) {
            throw new RuntimeException("Cette action a déjà été utilisée pour ce devis.");
        }

        Devis devis = devisRepository.findById(devisId)
                .orElseThrow(() -> new RuntimeException("Devis not found"));

        Visa visa = convertToEntity(dto);
        visa.setDevis(devis);
        return convertToDTO(visaRepository.save(visa));
    }

    @Override
    public VisaDTO updateVisa(Long id, VisaDTO dto) {
        Visa visa = visaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Visa not found"));
        visa.setAction(dto.getAction());
        visa.setName(dto.getName());
        visa.setDate(dto.getDate());
        visa.setVisa(dto.getVisa());
        return convertToDTO(visaRepository.save(visa));
    }

    @Override
    public void deleteVisa(Long id) {
        visaRepository.deleteById(id);
    }

    private VisaDTO convertToDTO(Visa visa) {
        return VisaDTO.builder()
                .id(visa.getId())
                .devisId(visa.getDevis().getId())
                .action(visa.getAction())
                .name(visa.getName())
                .date(visa.getDate())
                .visa(visa.getVisa())
                .build();
    }

    private Visa convertToEntity(VisaDTO dto) {
        return Visa.builder()
                .action(dto.getAction())
                .name(dto.getName())
                .date(dto.getDate())
                .visa(dto.getVisa())
                .build();
    }
}
