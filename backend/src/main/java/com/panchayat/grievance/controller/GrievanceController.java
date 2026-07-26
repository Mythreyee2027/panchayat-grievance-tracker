package com.panchayat.grievance.controller;

import com.panchayat.grievance.dto.GrievanceCreateRequest;
import com.panchayat.grievance.dto.GrievanceResponse;
import com.panchayat.grievance.model.Grievance;
import com.panchayat.grievance.repository.GrievanceRepository;
import com.panchayat.grievance.service.MlPredictionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/grievances")
public class GrievanceController {

    private static final List<String> CATEGORIES = List.of(
            "Water Supply", "Road/Culvert", "Street Light", "Drainage",
            "Sanitation", "Electricity", "Public Property");

    private static final Map<String, String> CATEGORY_DEPT = Map.of(
            "Water Supply", "Water Dept",
            "Road/Culvert", "Public Works",
            "Street Light", "Electrical Dept",
            "Drainage", "Public Works",
            "Sanitation", "Sanitation Dept",
            "Electricity", "Electrical Dept",
            "Public Property", "General Admin");

    private final GrievanceRepository repository;
    private final MlPredictionService mlPredictionService;

    public GrievanceController(GrievanceRepository repository, MlPredictionService mlPredictionService) {
        this.repository = repository;
        this.mlPredictionService = mlPredictionService;
    }

    // ---------- Task 2: create a grievance (server-side validation + storage) ----------
    @PostMapping
    public ResponseEntity<GrievanceResponse> create(@Valid @RequestBody GrievanceCreateRequest req) {
        Grievance g = new Grievance();
        g.setGrievanceId(nextGrievanceId());
        g.setComplainant(req.getComplainant().trim());
        g.setWard(req.getWard());
        g.setCategory(req.getCategory());
        g.setDepartment(CATEGORY_DEPT.getOrDefault(req.getCategory(), null)); // derived on the server
        g.setDescription(req.getDescription().trim());
        g.setDateRaised(LocalDate.now());
        g.setStatus("Open");

        // Task 4/5: ask the ML service for a delay-risk prediction using only
        // fields available right now. Never blocks saving if it fails.
        MlPredictionService.Prediction prediction = mlPredictionService.predict(g);
        if (prediction != null) {
            g.setPredictedDelayRisk(prediction.label);
            g.setPredictionConfidence(prediction.confidence);
        }

        Grievance saved = repository.save(g);
        return ResponseEntity.status(HttpStatus.CREATED).body(GrievanceResponse.from(saved));
    }

    // ---------- Task 3: list, search, filter, ordered by urgency ----------
    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "") String status,
            @RequestParam(required = false, defaultValue = "") String category,
            @RequestParam(required = false, defaultValue = "") String department) {

        List<Grievance> rows = repository.searchAndFilter(search, status, category, department);
        List<GrievanceResponse> data = rows.stream().map(GrievanceResponse::from).collect(Collectors.toList());

        Map<String, Object> body = new HashMap<>();
        body.put("count", data.size());
        body.put("results", data);
        return ResponseEntity.ok(body);
    }

    // ---------- dropdown options for the "new grievance" form and filters ----------
    @GetMapping("/meta")
    public ResponseEntity<Map<String, Object>> meta() {
        Map<String, Object> body = new HashMap<>();
        body.put("categories", CATEGORIES);
        body.put("statuses", List.of("Open", "In Progress", "Resolved"));
        body.put("departments", CATEGORY_DEPT.values().stream().distinct().collect(Collectors.toList()));
        return ResponseEntity.ok(body);
    }

    // Simple incremental id continuing from whatever is already in the table.
    private String nextGrievanceId() {
        long count = repository.count();
        return String.format("G%04d", count + 1);
    }

    // ---------- validation error handling (Task 2: server validates every field) ----------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                errors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }
}
