package com.workpilot.repository;

import com.workpilot.entity.Client;
import com.workpilot.entity.Devis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {
    @Query("SELECT c FROM Client c LEFT JOIN FETCH c.projects")
    List<Client> findAllWithProjects();

}