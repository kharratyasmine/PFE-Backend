package com.workpilot.repository.Psr;

import com.workpilot.entity.PSR.Deliveries;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveriesRepository extends JpaRepository<Deliveries, Long> {

    List<Deliveries> findByPsrId(Long psrId);
}