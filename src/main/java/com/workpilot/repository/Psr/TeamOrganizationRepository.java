package com.workpilot.repository.Psr;

import com.workpilot.entity.PSR.TeamOrganization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamOrganizationRepository extends JpaRepository<TeamOrganization, Long> {
    List<TeamOrganization> findByPsrId(Long psrId);
}
