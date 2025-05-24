package com.workpilot.entity.ressources;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(
        name = "work_entry",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"member_id", "task_id", "date"})
        }
)

public class WorkEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long taskId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String status; // 'full', 'half', 'leave', 'none'

    private String comment;



}