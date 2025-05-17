package com.workpilot.entity.devis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.workpilot.entity.ressources.Project;
import com.workpilot.entity.ressources.Demande;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "devis")
public class Devis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reference;
    private String edition;
    private LocalDate creationDate;
    private String status;
    private String proposalValidity;
    private String author;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "demande_id", nullable = false)
    private Demande demande;

    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<FinancialDetail> financialDetails;

    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<WorkloadDetail> workloadDetails;

    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<InvoicingDetail> invoicingDetails;

    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Visa> visas;

    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<DevisHistory> history;

    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Distribution> distributions;

    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<ProposalSummary> proposalSummary;
}
