package com.workpilot.entity.ressources;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

import java.lang.reflect.Member;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Table(
        name = "team_member_allocation",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"project_id", "team_member_id", "team_id"})
        }
)
public class TeamMemberAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double allocation;
    @ManyToOne
    @JoinColumn(name = "team_member_id")
    private TeamMember teamMember;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;


}