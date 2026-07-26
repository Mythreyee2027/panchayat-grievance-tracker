package com.panchayat.grievance.dto;

import com.panchayat.grievance.model.Grievance;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * What the frontend actually receives. daysOpen is the derived figure computed
 * on the server (Task 2 / Task 3): for still-open cases it is "today - date_raised",
 * for resolved cases it is the stored days_to_resolve. Never computed on the client,
 * so every user sees the same number.
 */
public class GrievanceResponse {
    private String grievanceId;
    private String complainant;
    private Integer ward;
    private String category;
    private String department; // may be null - the screen must handle that
    private String description;
    private String dateRaised;
    private String status;
    private String resolvedDate;
    private Integer daysOpen;
    private String predictedDelayRisk;   // null if no prediction / low confidence
    private Double predictionConfidence; // null if no prediction

    public static GrievanceResponse from(Grievance g) {
        GrievanceResponse r = new GrievanceResponse();
        r.grievanceId = g.getGrievanceId();
        r.complainant = g.getComplainant();
        r.ward = g.getWard();
        r.category = g.getCategory();
        r.department = g.getDepartment();
        r.description = g.getDescription();
        r.dateRaised = g.getDateRaised() != null ? g.getDateRaised().toString() : null;
        r.status = g.getStatus();
        r.resolvedDate = g.getResolvedDate() != null ? g.getResolvedDate().toString() : null;

        if ("Resolved".equals(g.getStatus())) {
            r.daysOpen = g.getDaysToResolve();
        } else if (g.getDateRaised() != null) {
            r.daysOpen = (int) ChronoUnit.DAYS.between(g.getDateRaised(), LocalDate.now());
        } else {
            r.daysOpen = null;
        }

        r.predictedDelayRisk = g.getPredictedDelayRisk();
        r.predictionConfidence = g.getPredictionConfidence();
        return r;
    }

    // getters (no setters needed - built only via from())
    public String getGrievanceId() { return grievanceId; }
    public String getComplainant() { return complainant; }
    public Integer getWard() { return ward; }
    public String getCategory() { return category; }
    public String getDepartment() { return department; }
    public String getDescription() { return description; }
    public String getDateRaised() { return dateRaised; }
    public String getStatus() { return status; }
    public String getResolvedDate() { return resolvedDate; }
    public Integer getDaysOpen() { return daysOpen; }
    public String getPredictedDelayRisk() { return predictedDelayRisk; }
    public Double getPredictionConfidence() { return predictionConfidence; }
}
