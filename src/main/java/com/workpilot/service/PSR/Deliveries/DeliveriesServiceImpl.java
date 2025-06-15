package com.workpilot.service.PSR.Deliveries;

import com.workpilot.dto.PsrDTO.DeliveriesDTO;
import com.workpilot.entity.PSR.Deliveries;
import com.workpilot.entity.PSR.Psr;
import com.workpilot.repository.Psr.DeliveriesRepository;
import com.workpilot.repository.Psr.PsrRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeliveriesServiceImpl implements DeliveriesService {

    @Autowired
    private DeliveriesRepository deliveryRepository;

    @Autowired
    private PsrRepository psrRepository;

    @Override
    public DeliveriesDTO addDeliveryToPsr(Long psrId, DeliveriesDTO deliveryDTO) {
        System.out.println("🟢 Tentative d’ajout de delivery pour PSR id: " + psrId);
        System.out.println("📦 Payload reçu: " + deliveryDTO);

        Psr psr = psrRepository.findById(psrId)
                .orElseThrow(() -> new EntityNotFoundException("PSR not found"));

        Deliveries delivery = new Deliveries();
        delivery.setDeliveriesName(deliveryDTO.getDeliveriesName());
        delivery.setDescription(deliveryDTO.getDescription());
        delivery.setVersion(deliveryDTO.getVersion());
        delivery.setPlannedDate(deliveryDTO.getPlannedDate());
        delivery.setEffectiveDate(deliveryDTO.getEffectiveDate());
        delivery.setStatus(deliveryDTO.getStatus());
        delivery.setDeliverySupport(deliveryDTO.getDeliverySupport());
        delivery.setCustomerFeedback(deliveryDTO.getCustomerFeedback());
        delivery.setPsr(psr);
        delivery.setWeek(psr.getWeek());
        Deliveries saved = deliveryRepository.save(delivery);

        System.out.println("✅ Delivery enregistré avec id : " + saved.getId());
        return convertToDTO(saved);
    }


    @Override
    public List<DeliveriesDTO> getDeliveriesByPsrId(Long psrId) {
        return deliveryRepository.findByPsrId(psrId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

    }


    @Override
    public void deleteDelivery(Long id) {
        deliveryRepository.deleteById(id);
    }

    private DeliveriesDTO convertToDTO(Deliveries delivery) {
        DeliveriesDTO dto = new DeliveriesDTO();
        dto.setId(delivery.getId());
        dto.setDeliveriesName(delivery.getDeliveriesName());
        dto.setDescription(delivery.getDescription());
        dto.setPlannedDate(delivery.getPlannedDate());
        dto.setEffectiveDate(delivery.getEffectiveDate());
        dto.setVersion(delivery.getVersion());
        dto.setStatus(delivery.getStatus());
        dto.setDeliverySupport(delivery.getDeliverySupport());
        dto.setCustomerFeedback(delivery.getCustomerFeedback());
        return dto;
    }

}