package com.workpilot.service.GestionRessources.PlannedWorkloadMember;

import com.workpilot.dto.GestionRessources.PlannedWorkloadMemberDTO;
import com.workpilot.entity.ressources.*;
import com.workpilot.repository.ressources.PlannedWorkloadMemberRepository;
import com.workpilot.repository.ressources.ProjectRepository;
import com.workpilot.repository.ressources.TeamMemberAllocationRepository;
import com.workpilot.repository.ressources.TeamMemberRepository;
import com.workpilot.service.PublicHolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlannedWorkloadMemberServiceImpl implements PlannedWorkloadMemberService {

    @Autowired
    private PlannedWorkloadMemberRepository repository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private TeamMemberAllocationRepository teamMemberAllocationRepository;
    @Autowired
    private PublicHolidayService publicHolidayService;

    @Override
    public PlannedWorkloadMemberDTO save(PlannedWorkloadMemberDTO dto) {
        PlannedWorkloadMember entity = mapToEntity(dto);
        return toDTO(repository.save(entity));
    }

    @Override
    public PlannedWorkloadMemberDTO update(Long id, PlannedWorkloadMemberDTO dto) {
        PlannedWorkloadMember existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Planned workload not found"));

        existing.setMonth(String.valueOf(dto.getMonth()));
        existing.setYear(dto.getYear());
        existing.setWorkload((int) dto.getWorkload());
        existing.setNote(dto.getNote());

        return toDTO(repository.save(existing));
    }

    @Override
    public List<PlannedWorkloadMemberDTO> getByProject(Long projectId) {
        return repository.findByProjectId(projectId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<PlannedWorkloadMemberDTO> getByProjectAndYear(Long projectId, int year) {
        return repository.findByProjectIdAndYear(projectId, year)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<PlannedWorkloadMemberDTO> getByMember(Long memberId, Long projectId) {
        return repository.findByProjectIdAndTeamMemberId(projectId, memberId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }
    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private int safeParseMonth(String monthStr) {
        try {
            return Integer.parseInt(monthStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private PlannedWorkloadMemberDTO toDTO(PlannedWorkloadMember entity) {
        return PlannedWorkloadMemberDTO.builder()
                .id(entity.getId())
                .month(safeParseMonth(entity.getMonth()))
                .year(entity.getYear())
                .workload(entity.getWorkload())
                .note(entity.getNote())
                .teamMemberId(entity.getTeamMember() != null ? entity.getTeamMember().getId() : null)
                .teamMemberName(entity.getTeamMember() != null ? entity.getTeamMember().getName() : "N/A")
                .teamMemberRole(entity.getTeamMember() != null && entity.getTeamMember().getRole() != null ? entity.getTeamMember().getRole().name() : "UNKNOWN")
                .projectId(entity.getProject() != null ? entity.getProject().getId() : null)
                .build();
    }

    private PlannedWorkloadMember mapToEntity(PlannedWorkloadMemberDTO dto) {
        PlannedWorkloadMember entity = new PlannedWorkloadMember();
        entity.setId(dto.getId());
        entity.setMonth(String.valueOf(dto.getMonth()));
        entity.setYear(dto.getYear());
        entity.setWorkload((int) dto.getWorkload());
        entity.setNote(dto.getNote());

        if (dto.getTeamMemberId() != null && dto.getTeamMemberId() > 0) {
            TeamMember member = teamMemberRepository.findById(dto.getTeamMemberId())
                    .orElseThrow(() -> new RuntimeException("Team member not found"));
            entity.setTeamMember(member);
        } else {
            TeamMember simulated = new TeamMember();
            simulated.setId(dto.getTeamMemberId());
            simulated.setName(dto.getTeamMemberName());
            simulated.setInitial("AUTO");
            simulated.setRole(dto.getTeamMemberRole() != null ? Seniority.valueOf(dto.getTeamMemberRole()) : null);
            simulated.setJobTitle("Fictif");
            simulated.setNote("Fake Member");
            simulated.setCost(estimateCostByRole(simulated.getRole()));
            entity.setTeamMember(simulated);
        }

        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        entity.setProject(project);
        return entity;
    }

    private double estimateCostByRole(Seniority role) {
        return switch (role) {
            case JUNIOR -> 200;
            case INTERMEDIAIRE -> 350;
            case SENIOR -> 500;
            case SENIOR_MANAGER -> 800;
        };
    }


    @Override
    public List<PlannedWorkloadMemberDTO> generateWorkloadsForProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        if (project.getDemandes() == null || project.getDemandes().isEmpty()) {
            throw new RuntimeException("Aucune demande liée à ce projet");
        }

        List<TeamMemberAllocation> allocations = teamMemberAllocationRepository.findAllByProjectId(projectId);
        List<PlannedWorkloadMemberDTO> generated = new ArrayList<>();

        for (Demande demande : project.getDemandes()) {
            LocalDate startDate = demande.getDateDebut();
            LocalDate endDate = demande.getDateFin();

            for (TeamMemberAllocation allocationEntity : allocations) {
                TeamMember member = allocationEntity.getTeamMember();
                double allocation = allocationEntity.getAllocation();

                generateWorkloads(generated, project, demande, member, allocation, allocationEntity.getTeam().getName());
            }

            if (demande.getFakeMembers() != null) {
                for (FakeMember fake : demande.getFakeMembers()) {
                    TeamMember temp = createTemporaryMemberFromFake(fake);
                    generateWorkloads(generated, project, demande, temp, 1.0, "FakeTeam");
                }
            }
        }

        return generated;
    }

    private void generateWorkloads(List<PlannedWorkloadMemberDTO> result, Project project, Demande demande,
                                   TeamMember member, double allocation, String teamName) {
        LocalDate current = demande.getDateDebut().withDayOfMonth(1);
        LocalDate end = demande.getDateFin().withDayOfMonth(1);

        while (!current.isAfter(end)) {
            int year = current.getYear();
            int month = current.getMonthValue();

            int workdays = calculateWorkdays(year, month);
            int workload = (int) (workdays * allocation);

            PlannedWorkloadMember entity = new PlannedWorkloadMember();
            entity.setProject(project);
            entity.setTeamMember(member);
            entity.setYear(year);
            entity.setMonth(String.valueOf(month));
            entity.setWorkload(workload);
            entity.setNote("Auto-generated for demande [" + demande.getName() + "] - Team: " + teamName);

            repository.save(entity);
            result.add(toDTO(entity));
            current = current.plusMonths(1);
        }
    }

    private TeamMember createTemporaryMemberFromFake(FakeMember fake) {
        TeamMember temp = new TeamMember();
        temp.setId((long) -Math.abs(Objects.hash(fake.getName(), fake.getInitial())));
        temp.setName(fake.getName());
        temp.setInitial(fake.getInitial());
        temp.setNote(fake.getNote());
        temp.setRole(Seniority.valueOf(fake.getRole()));
        temp.setCost(estimateCostByRole(Seniority.valueOf(fake.getRole())));
        temp.setStartDate(LocalDate.now());
        temp.setHoliday(new ArrayList<>());
        temp.setImage("assets/img/profiles/default-avatar.jpg");
        temp.setExperienceRange("-");
        temp.setTeams(new HashSet<>());
        return temp;
    }


    private int calculateWorkdays(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        Set<LocalDate> publicHolidays = publicHolidayService.getAllCombinedHolidays(year);

        int workdays = 0;
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            if (date.getDayOfWeek().getValue() <= 5 &&
                    !publicHolidays.contains(date)) {
                workdays++;
            }
        }
        return workdays;
    }


    private Set<LocalDate> getMemberHolidays(TeamMember member) {
        Set<LocalDate> holidays = new HashSet<>();
        if (member.getHoliday() != null) {
            for (String dateStr : member.getHoliday()) {
                holidays.add(LocalDate.parse(dateStr));
            }
        }
        return holidays;
    }

    @Override
    public void generateForMember(Long projectId, Long memberId, double allocationInput) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        if (project.getDemandes() == null || project.getDemandes().isEmpty()) {
            throw new RuntimeException("Aucune demande liée à ce projet");
        }

        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Membre introuvable"));

        // 🔍 Récupère toutes les allocations de ce membre sur ce projet
        List<TeamMemberAllocation> allocations = teamMemberAllocationRepository
                .findAllByProjectIdAndTeamMemberId(projectId, memberId);

        // 🔄 BOUCLE SUR TOUTES LES DEMANDES
        for (Demande demande : project.getDemandes()) {

            LocalDate startDate = demande.getDateDebut();
            LocalDate endDate = demande.getDateFin();

            for (TeamMemberAllocation allocationEntity : allocations) {
                double allocation = allocationEntity.getAllocation();

                LocalDate current = startDate.withDayOfMonth(1);
                LocalDate end = endDate.withDayOfMonth(1);

                while (!current.isAfter(end)) {
                    int year = current.getYear();
                    int month = current.getMonthValue();

                    int workdays = calculateWorkdays(year, month);
                    int workload = (int) (workdays * allocation);

                    // 🔍 Check si existe déjà
                    Optional<PlannedWorkloadMember> existingOpt = repository
                            .findByProjectIdAndTeamMemberIdAndYearAndMonth(projectId, memberId, year, String.valueOf(month));

                    PlannedWorkloadMember entity;
                    if (existingOpt.isPresent()) {
                        entity = existingOpt.get();
                        entity.setWorkload(workload);
                    } else {
                        entity = new PlannedWorkloadMember();
                        entity.setProject(project);
                        entity.setTeamMember(member);
                        entity.setYear(year);
                        entity.setMonth(String.valueOf(month));
                        entity.setWorkload(workload);
                        entity.setNote("Auto-generated for demande [" + demande.getName() + "] - Team: " + allocationEntity.getTeam().getName());
                        // 👉 Optionnel : entity.setTeam(allocationEntity.getTeam());
                    }

                    repository.save(entity);
                    current = current.plusMonths(1);
                }
            }
        }
    }

    @Override
    public void deleteWorkloadsByProjectAndMember(Long projectId, Long memberId) {
        // Récupérer les workloads planifiés pour le membre et le projet
        List<PlannedWorkloadMember> workloads = repository.findByProjectIdAndTeamMemberId(projectId, memberId);

        // Vérifier si des workloads ont été trouvés
        if (!workloads.isEmpty()) {
            // Supprimer tous les workloads associés
            repository.deleteAll(workloads);
        } else {
            // Si aucun workload n'a été trouvé, vous pouvez décider d'une action, comme enregistrer un log ou lancer une exception
            System.out.println("Aucun workload trouvé pour le projet " + projectId + " et le membre " + memberId);
        }
    }



}
