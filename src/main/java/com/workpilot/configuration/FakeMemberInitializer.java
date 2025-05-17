package com.workpilot.configuration;

import com.workpilot.entity.ressources.TeamMember;
import com.workpilot.entity.ressources.Seniority;
import com.workpilot.repository.ressources.TeamMemberRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FakeMemberInitializer {

    private final TeamMemberRepository teamMemberRepository;
    @PostConstruct
    public void initFakeMembers() {
        List<Seniority> roles = List.of(
                Seniority.JUNIOR,
                Seniority.INTERMEDIAIRE,
                Seniority.SENIOR,
                Seniority.SENIOR_MANAGER
        );

        for (Seniority role : roles) {
            boolean exists = teamMemberRepository.existsByFakeTrueAndRole(role);
            if (!exists) {
                TeamMember fake = new TeamMember();
                fake.setName("Inconnu (" + role.name() + ")");
                fake.setInitial("INC_" + role.name().substring(0, 3));
                fake.setRole(role);
                fake.setCost(estimateCostByRole(role));
                fake.setFake(true); // ✅ CORRECT
                fake.setImage("assets/img/profiles/default-avatar.jpg");
                fake.setHoliday(new ArrayList<>());
                fake.setStartDate(LocalDate.now());
                fake.setExperienceRange("-");
                fake.setNote("Fake member auto-created");

                teamMemberRepository.save(fake);
            }
        }
    }


    private double estimateCostByRole(Seniority role) {
        return switch (role) {
            case JUNIOR -> 200;
            case INTERMEDIAIRE -> 350;
            case SENIOR -> 500;
            case SENIOR_MANAGER -> 800;
        };
    }
}
