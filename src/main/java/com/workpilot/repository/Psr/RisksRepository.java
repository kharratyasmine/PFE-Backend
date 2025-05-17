package com.workpilot.repository.Psr;

import com.workpilot.entity.PSR.Risks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RisksRepository extends JpaRepository<Risks, Long> {

    List<Risks> findByPsrId(Long psrId);
}
