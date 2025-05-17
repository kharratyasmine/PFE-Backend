package com.workpilot.service.PSR.Deliveries;

import com.workpilot.dto.PsrDTO.DeliveriesDTO;

import java.util.List;

public interface DeliveriesService {
    DeliveriesDTO addDeliveryToPsr(Long psrId, DeliveriesDTO deliveryDTO);
    List<DeliveriesDTO> getDeliveriesByPsrId(Long psrId);
    void deleteDelivery(Long id);
}
