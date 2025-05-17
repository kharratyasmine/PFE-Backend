package com.workpilot.entity.ressources;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "TeamMember")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String image;
    private String name;
    private String initial;

    private String jobTitle;
    private String experienceRange;

    @Enumerated(EnumType.STRING)
    private Seniority role;
    private Boolean fake = false;
    private Double cost;
    private String note;
    private LocalDate StartDate;
    private LocalDate EndDate;
    private String status; // "En poste" ou "Inactif"



    @ElementCollection
    @CollectionTable(name = "team_member_holidays", joinColumns = @JoinColumn(name = "team_member_id"))
    @Column(name = "holiday")
    private List<String> holiday = new ArrayList<>();

    @ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Team> teams = new HashSet<>();


    @OneToMany(mappedBy = "teamMember", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TeamMemberAllocation> allocations= new HashSet<>();


}
