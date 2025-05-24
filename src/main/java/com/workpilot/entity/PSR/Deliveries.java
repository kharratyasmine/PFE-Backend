package com.workpilot.entity.PSR;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;


@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "deliveries")
public class Deliveries{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String deliveriesName;
        private String description;
        private LocalDate plannedDate;
        private LocalDate effectiveDate;
        private String version ;
        private String status; // Delivered, Pending, Late
        private String deliverySupport ;
        private String customerFeedback;
    @Column(name = "week")
    private String week;

    @Column(name = "report_year")
    private Integer reportYear;
        @ManyToOne
        private Psr psr;
    }