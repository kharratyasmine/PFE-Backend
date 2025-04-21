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
        name = "TeamMemberAllocation",
        uniqueConstraints = @UniqueConstraint(columnNames = {"team_member_id", "project_id"})
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


}