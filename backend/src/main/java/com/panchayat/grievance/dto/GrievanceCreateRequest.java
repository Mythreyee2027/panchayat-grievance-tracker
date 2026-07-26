package com.panchayat.grievance.dto;

import jakarta.validation.constraints.*;

public class GrievanceCreateRequest {

    @NotBlank(message = "Complainant name is required")
    @Size(max = 100, message = "Complainant name must be under 100 characters")
    private String complainant;

    @NotNull(message = "Ward is required")
    @Min(value = 1, message = "Ward must be between 1 and 15")
    @Max(value = 15, message = "Ward must be between 1 and 15")
    private Integer ward;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must be under 500 characters")
    private String description;

    // getters / setters
    public String getComplainant() { return complainant; }
    public void setComplainant(String complainant) { this.complainant = complainant; }

    public Integer getWard() { return ward; }
    public void setWard(Integer ward) { this.ward = ward; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
