package com.workpilot.entity.devis;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.workpilot.entity.ressources.Project;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
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
    private String proposalValidity; // Période de validité du devis
    private String author;

    @ManyToOne
    @JsonIgnoreProperties("devis")
    private Project project;

    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<FinancialDetail> financialDetails;

    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<WorkloadDetail> workloadDetails;

    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<InvoicingDetail> invoicingDetails;

    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Visa> visas;

    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<DevisHistory> history;

    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL)
    @JsonBackReference
    private List<Distribution> distributions;

    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL)
    @JsonBackReference
    private List<ProposalSummary>  proposalSummary;
}
