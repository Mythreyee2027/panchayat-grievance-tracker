package com.panchayat.grievance.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * One row = one grievance.
 * Field meanings match the dataset documented in data/generate_dataset.py.
 */
@Entity
@Table(name = "grievances")
public class Grievance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "grievance_id", unique = true, nullable = false)
    private String grievanceId;

    @Column(nullable = false)
    private String complainant;

    @Column(nullable = false)
    private Integer ward;

    @Column(nullable = false)
    private String category;

    // Nullable on purpose - Task 1's awkward case (G0096) has no department,
    // and the screen must not crash when this is missing.
    private String department;

    @Column(length = 500)
    private String description;

    @Column(name = "date_raised", nullable = false)
    private LocalDate dateRaised;

    @Column(nullable = false)
    private String status; // Open | In Progress | Resolved

    @Column(name = "resolved_date")
    private LocalDate resolvedDate;

    @Column(name = "days_to_resolve")
    private Integer daysToResolve;

    // Outcome column used to train the Task 4 model. Null until resolved.
    private Integer delayed;

    // ----- prediction fields, filled in by the ML service at creation time (Task 5) -----
    @Column(name = "predicted_delay_risk")
    private String predictedDelayRisk; // "Likely on time" | "At risk of delay" | null

    @Column(name = "prediction_confidence")
    private Double predictionConfidence;

    public Grievance() {}

    // ----- getters and setters -----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGrievanceId() { return grievanceId; }
    public void setGrievanceId(String grievanceId) { this.grievanceId = grievanceId; }

    public String getComplainant() { return complainant; }
    public void setComplainant(String complainant) { this.complainant = complainant; }

    public Integer getWard() { return ward; }
    public void setWard(Integer ward) { this.ward = ward; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDateRaised() { return dateRaised; }
    public void setDateRaised(LocalDate dateRaised) { this.dateRaised = dateRaised; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getResolvedDate() { return resolvedDate; }
    public void setResolvedDate(LocalDate resolvedDate) { this.resolvedDate = resolvedDate; }

    public Integer getDaysToResolve() { return daysToResolve; }
    public void setDaysToResolve(Integer daysToResolve) { this.daysToResolve = daysToResolve; }

    public Integer getDelayed() { return delayed; }
    public void setDelayed(Integer delayed) { this.delayed = delayed; }

    public String getPredictedDelayRisk() { return predictedDelayRisk; }
    public void setPredictedDelayRisk(String predictedDelayRisk) { this.predictedDelayRisk = predictedDelayRisk; }

    public Double getPredictionConfidence() { return predictionConfidence; }
    public void setPredictionConfidence(Double predictionConfidence) { this.predictionConfidence = predictionConfidence; }
}
