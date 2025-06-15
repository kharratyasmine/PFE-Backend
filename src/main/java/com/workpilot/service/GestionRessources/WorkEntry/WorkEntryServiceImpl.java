package com.workpilot.service.GestionRessources.WorkEntry;

import com.workpilot.entity.ressources.WorkEntry;
import com.workpilot.entity.ressources.TeamMember;
import com.workpilot.repository.ressources.WorkEntryRepository;
import com.workpilot.repository.ressources.TeamMemberRepository;
import com.workpilot.service.GestionRessources.tache.ProjectTaskServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Service
public class WorkEntryServiceImpl implements WorkEntryService {

    @Autowired
    private WorkEntryRepository workEntryRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private ProjectTaskServiceImpl projectTaskService; // Pour utiliser la méthode processWorkEntryAsHoliday

    @Override
    public List<WorkEntry> getWorkEntriesByTask(Long taskId) {
        return workEntryRepository.findByTaskId(taskId);
    }

    @Override
    public List<WorkEntry> getWorkEntriesByMember(Long memberId) {
        return workEntryRepository.findByMemberId(memberId);
    }

    @Override
    public List<WorkEntry> getWorkEntriesByMemberAndTask(Long memberId, Long taskId) {
        return workEntryRepository.findByMemberIdAndTaskId(memberId, taskId);
    }

    @Override
    @Transactional
    public WorkEntry createWorkEntry(WorkEntry workEntry) {
        // Vérifier l'existence d'une entrée similaire
        List<WorkEntry> existingEntries = workEntryRepository.findByMemberIdAndTaskIdAndDate(
                workEntry.getMemberId(),
                workEntry.getTaskId(),
                workEntry.getDate()
        );

        WorkEntry savedEntry;

        if (!existingEntries.isEmpty()) {
            // Mettre à jour l'entrée existante au lieu d'en créer une nouvelle
            WorkEntry existingEntry = existingEntries.get(0);
            existingEntry.setStatus(workEntry.getStatus());
            existingEntry.setComment(workEntry.getComment());
            savedEntry = workEntryRepository.save(existingEntry);
        } else {
            savedEntry = workEntryRepository.save(workEntry);
        }

        // 🎯 Traitement automatique des congés
        processHolidayForWorkEntry(savedEntry);

        return savedEntry;
    }

    @Override
    @Transactional
    public WorkEntry updateWorkEntry(Long id, WorkEntry workEntry) {
        workEntry.setId(id);
        WorkEntry savedEntry = workEntryRepository.save(workEntry);

        // 🎯 Traitement automatique des congés lors de la mise à jour
        processHolidayForWorkEntry(savedEntry);

        return savedEntry;
    }

    @Override
    public void deleteWorkEntry(Long id) {
        workEntryRepository.deleteById(id);
    }

    @Override
    public WorkEntry getWorkEntryById(Long id) {
        return workEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("WorkEntry not found with id: " + id));
    }

    /**
     * Traite automatiquement les congés basés sur le status de la WorkEntry
     * Si status < 1.0, enregistre comme congé dans team_member_holiday
     */
    @Transactional
    private void processHolidayForWorkEntry(WorkEntry workEntry) {
        // Si le status est >= 1.0, c'est un jour de travail normal, pas de congé
        if (workEntry.getStatus() >= 1.0) {
            // Optionnel : supprimer un congé existant pour cette date si présent
            removeHolidayIfExists(workEntry.getMemberId(), String.valueOf(workEntry.getDate()));
            return;
        }

        // Récupérer le membre
        TeamMember member = teamMemberRepository.findById(workEntry.getMemberId())
                .orElseThrow(() -> new RuntimeException("Membre non trouvé avec l'ID: " + workEntry.getMemberId()));

        // Initialiser la liste des congés si elle n'existe pas
        if (member.getHoliday() == null) {
            member.setHoliday(new ArrayList<>());
        }

        // Créer l'entrée de congé avec le label approprié
        String holidayLabel = getHolidayLabel(workEntry.getStatus());
        String holidayEntry = workEntry.getDate() + "|" + holidayLabel;

        // Utiliser un Set pour éviter les doublons
        Set<String> existingHolidays = new HashSet<>(member.getHoliday());

        // Supprimer toute entrée existante pour cette date
        existingHolidays.removeIf(entry -> entry.startsWith(workEntry.getDate() + "|"));

        // Ajouter la nouvelle entrée
        existingHolidays.add(holidayEntry);

        // Mettre à jour la liste des congés
        member.setHoliday(new ArrayList<>(existingHolidays));

        // Sauvegarder le membre
        teamMemberRepository.save(member);

        System.out.println("✅ Congé enregistré : " + holidayEntry + " pour le membre ID: " + workEntry.getMemberId());
    }

    /**
     * Supprime un congé existant pour une date donnée si le membre travaille normalement
     */
    @Transactional
    private void removeHolidayIfExists(Long memberId, String date) {
        TeamMember member = teamMemberRepository.findById(memberId)
                .orElse(null);

        if (member != null && member.getHoliday() != null) {
            boolean removed = member.getHoliday().removeIf(entry -> entry.startsWith(date + "|"));

            if (removed) {
                teamMemberRepository.save(member);
                System.out.println("❌ Congé supprimé pour la date: " + date + " - Membre ID: " + memberId);
            }
        }
    }

    /**
     * Convertit le status numérique en label de congé
     */
    private String getHolidayLabel(double status) {
        if (status == 0.0) return "CONGE_TOTAL";
        if (status == 0.25) return "QUART";
        if (status == 0.5) return "DEMI_JOURNEE";
        if (status == 0.75) return "TROIS_QUARTS";
        return "PARTIEL"; // Pour tout autre valeur < 1.0
    }

    /**
     * Méthode alternative utilisant la logique existante de ProjectTaskServiceImpl
     * Si vous préférez utiliser cette approche
     */
    @Transactional
    public WorkEntry createWorkEntryWithHolidayProcessing(WorkEntry workEntry) {
        WorkEntry savedEntry = createWorkEntry(workEntry);

        // Utiliser la méthode existante de ProjectTaskServiceImpl
        projectTaskService.processWorkEntryAsHoliday(savedEntry);

        return savedEntry;
    }
}