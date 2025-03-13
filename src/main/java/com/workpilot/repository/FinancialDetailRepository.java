package com.workpilot.repository;

import com.workpilot.entity.FinancialDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialDetailRepository extends JpaRepository<FinancialDetail, Long> {
}
