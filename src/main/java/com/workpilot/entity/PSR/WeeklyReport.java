package com.workpilot.entity.PSR;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// WeeklyReport.java
@Entity
@Table(name = "weekly_reports")
@Getter
@Setter
public class WeeklyReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "psr_id")
    private Psr psr;

    private String projectName;
    private Double workingDays;
    private Double estimatedDays;
    private Double effortVariance;
    private String week;

    // Getters, setters, constructors
}

/*
// Action.java
@Entity
@Table(name = "actions")
public class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "psr_id")
    private Psr psr;

    private LocalDate date;
    private String type;
    private String risk;
    private String who;
    private String what;
    private LocalDate when;
    private String status;
    private String owner;
    private String priority;
    private String comments;

    // Getters, setters, constructors
}*/
