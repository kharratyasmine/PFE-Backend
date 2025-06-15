package com.workpilot.service.DevisServices.DevisDistribution;

import com.workpilot.dto.DevisDTO.DistributionDTO;
import com.workpilot.entity.devis.Devis;
import com.workpilot.entity.devis.Distribution;
import com.workpilot.repository.devis.DevisRepository;

import com.workpilot.repository.devis.DistributionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DistributionServiceImpl implements DistributionService {

    @Autowired
    private DistributionRepository distributionRepository;

    @Autowired
    private DevisRepository devisRepository;
    @Override
    public List<DistributionDTO> getDistributionsByDevisId(Long devisId) {
        Devis devis = devisRepository.findById(devisId)
                .orElseThrow(() -> new RuntimeException("Devis not found"));

        // 1. Charger depuis la base les distributions déjà enregistrées
        List<Distribution> existingDistributions = distributionRepository.findByDevisId(devisId);
        List<String> existingNames = existingDistributions.stream()
                .map(Distribution::getName)
                .collect(Collectors.toList());

        List<DistributionDTO> result = existingDistributions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 2. Générer dynamiquement celles qui manquent côté client
        List<String> clientNames = devis.getProject().getClient().getSalesManagers();

        for (String name : clientNames) {
            if (!existingNames.contains(name)) {
                result.add(DistributionDTO.builder()
                        .name(name)
                        .devisId(devisId)
                        .partial(false)
                        .complete(false)
                        .function("")
                        .type("Customer") // ✅ Important
                        .build());
            }
        }

        // 3. Ajouter l'utilisateur du projet (TELNET)
        String userFullName = devis.getProject().getUser().getFirstname() + " " + devis.getProject().getUser().getLastname();
        if (!existingNames.contains(userFullName)) {
            result.add(DistributionDTO.builder()
                    .name(userFullName)
                    .devisId(devisId)
                    .partial(false)
                    .complete(false)
                    .function("")
                    .type("Telnet") // ✅ Important
                    .build());
        }

        return result;
    }



    @Override
    public DistributionDTO addDistribution(Long devisId, DistributionDTO distributionDTO) {
        Devis devis = devisRepository.findById(devisId)
                .orElseThrow(() -> new RuntimeException("Devis not found"));

        Distribution distribution = new Distribution();
        distribution.setDevis(devis);
        distribution.setName(distributionDTO.getName());
        distribution.setFunction(distributionDTO.getFunction());
        distribution.setPartial(distributionDTO.getPartial());
        distribution.setComplete(distributionDTO.getComplete());
        distribution.setType(determineType(distributionDTO.getName(), devis)); // 👈 intelligent



        Distribution savedDistribution = distributionRepository.save(distribution);

        return convertToDTO(savedDistribution);
    }

    @Override
    public DistributionDTO updateDistribution(Long distributionId, DistributionDTO distributionDTO) {
        Distribution distribution = distributionRepository.findById(distributionId)
                .orElseThrow(() -> new RuntimeException("Distribution not found"));

        distribution.setName(distributionDTO.getName());
        distribution.setFunction(distributionDTO.getFunction());
        distribution.setPartial(distributionDTO.getPartial());
        distribution.setComplete(distributionDTO.getComplete());
        distribution.setType(distributionDTO.getType()); // ✅ essentiel


        Distribution updatedDistribution = distributionRepository.save(distribution);

        return convertToDTO(updatedDistribution);
    }

    private DistributionDTO convertToDTO(Distribution distribution) {
        Devis devis = distribution.getDevis();
        String from = determineType(distribution.getName(), devis);

        return DistributionDTO.builder()
                .id(distribution.getId())
                .name(distribution.getName())
                .function(distribution.getFunction())
                .partial(distribution.isPartial())
                .complete(distribution.isComplete())
                .devisId(distribution.getDevis().getId())
                .type(distribution.getType())
                .build();
    }

    private String determineType(String name, Devis devis) {
        List<String> clientNames = devis.getProject().getClient().getSalesManagers();
        String userFullName = devis.getProject().getUser().getFirstname() + " " + devis.getProject().getUser().getLastname();

        if (clientNames.contains(name)) return "Customer";
        if (userFullName.equals(name)) return "Telnet";
        return "unknown";
    }



}

