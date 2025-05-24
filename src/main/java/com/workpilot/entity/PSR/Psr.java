package com.workpilot.entity.PSR;

import com.workpilot.entity.auth.User;
import com.workpilot.entity.devis.Devis;
import com.workpilot.entity.ressources.Project;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "psr", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"project_id", "week", "report_year"})
})
@Entity
public class Psr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reportTitle;
    private LocalDate reportDate;
    private String comments;
    private String overallStatus;
    private String reference;
    private String edition;
    private String preparedBy;
    private String approvedBy;
    private String validatedBy;
    private LocalDate PreparedByDate;
    private LocalDate approvedByDate;
    private LocalDate validatedByDate;
    private String projectName;
    private String week;
    private String clientName;

    @Column(name = "report_year")
    private Integer reportYear;

    // 🔹 Nom affiché textuellement
    private String authorName;

    @ManyToOne
    private Project project;

    @OneToMany(mappedBy = "psr", cascade = CascadeType.ALL)
    private List<Risks> risks;

    @OneToMany(mappedBy = "psr", cascade = CascadeType.ALL)
    private List<Deliveries> deliveries;

    @OneToMany(mappedBy = "psr", cascade = CascadeType.ALL)
    private List<TeamOrganization> teamOrganizations;


    @OneToMany(mappedBy = "psr", cascade = CascadeType.ALL)
    private List<WeeklyReport> weeklyReports;
}
