package com.workpilot.Export;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ConsolidatedWeeklyWorkload {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String month;
    private String projectNameWithWeek; // e.g., "workpilot (W23)"
    private double workingDays;
    private double estimatedDays;
    private double effortVariance;

    public ConsolidatedWeeklyWorkload(String month, String projectNameWithWeek, double workingDays, double estimatedDays, double effortVariance) {
        this.month = month;
        this.projectNameWithWeek = projectNameWithWeek;
        this.workingDays = workingDays;
        this.estimatedDays = estimatedDays;
        this.effortVariance = effortVariance;
    }
}