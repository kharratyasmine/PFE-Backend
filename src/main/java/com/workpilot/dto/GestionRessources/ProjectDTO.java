package com.workpilot.dto.GestionRessources;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.workpilot.dto.DevisDTO.DevisDTO;
import com.workpilot.entity.ressources.Status;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProjectDTO {

    private Long id;
    private String name;
    private String projectType;
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private Status status;

    private String activity;
    private String technologie;

    private Long clientId;
    private ClientDTO client; // ✅ Client complet

    private Long userId;
    private String userName;
    private UserDTO user;


    private List<TeamDTO> teams;     // Pour l'affichage
    private List<Long> teamIds;      // Pour l'enregistrement
    private List<DemandeDTO> demandes;
    private List<DevisDTO> devisList;

    // 🔧 Constructeur principal avec client (recommandé)
    public ProjectDTO(Long id, String name, String projectType, String description,
                      LocalDate startDate, LocalDate endDate,
                      String activity, String technologie,
                      Status status, ClientDTO client,
                      Long userId, String userName,
                      List<TeamDTO> teams, List<DemandeDTO> demandes, List<DevisDTO> devisList) {

        this.id = id;
        this.name = name;
        this.projectType = projectType;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.activity = activity;
        this.technologie = technologie;
        this.status = status;
        this.client = client;
        this.userId = userId;
        this.userName = userName;
        this.teams = teams;
        this.demandes = demandes;
        this.devisList = devisList;

        // Auto-remplir les teamIds
        if (teams != null) {
            this.teamIds = teams.stream()
                    .map(TeamDTO::getId)
                    .toList();
        }
    }

    // ✅ Autre constructeur simple si tu ne veux pas toute la structure
    public ProjectDTO(Long id, String name, String projectType, String description,
                      LocalDate startDate, LocalDate endDate,
                      String activity, String technologie,
                      Status status, Long userId, List<Long> teamIds) {

        this.id = id;
        this.name = name;
        this.projectType = projectType;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.activity = activity;
        this.technologie = technologie;
        this.status = status;
        this.userId = userId;
        this.teamIds = teamIds;
    }
}
