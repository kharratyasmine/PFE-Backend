package com.workpilot.repository.ressources;

import com.workpilot.entity.ressources.Demande;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

       @Modifying
       @Transactional
       @Query("DELETE FROM Demande d WHERE d.project.id = :projectId")
       void deleteByProjectId(@Param("projectId") Long projectId);

}
