package com.workpilot.repository.ressources;

import com.workpilot.entity.ressources.Demande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandeRepository extends JpaRepository<Demande, Long> {
       List<Demande> findByProjectId(Long projectId);

       @Query(value = "SELECT d.* FROM demande d " +
               "JOIN demandes_team_members dtm ON d.id = dtm.demande_id " +
               "WHERE dtm.team_members_id = :memberId",
               nativeQuery = true)
       List<Demande> findDemandesByTeamMemberId(@Param("memberId") Long memberId);
}
