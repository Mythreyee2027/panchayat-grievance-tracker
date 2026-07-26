package com.panchayat.grievance.service;

import com.panchayat.grievance.model.Grievance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

/**
 * Task 4/5: calls the Flask prediction microservice at grievance-creation time,
 * using ONLY fields that are available the moment the grievance is raised
 * (ward, category, department, day of week, description length) - never
 * status/resolved_date/days_to_resolve, since those don't exist yet for a new case.
 *
 * If the service is unreachable, or returns a low-confidence prediction, we store
 * nothing rather than forcing a guess - see Task 5 "no forced prediction" rule.
 */
@Service
public class MlPredictionService {

    private static final Logger log = LoggerFactory.getLogger(MlPredictionService.class);
    private static final double CONFIDENCE_THRESHOLD = 0.60;

    private final RestClient restClient;

    public MlPredictionService(@Value("${app.ml-service-url}") String mlServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(mlServiceUrl).build();
    }

    public static class Prediction {
        public String label;       // "Likely on time" | "At risk of delay"
        public Double confidence;  // 0.0 - 1.0
    }

    /** Returns null if prediction unavailable or below the confidence threshold. */
    public Prediction predict(Grievance g) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("ward", g.getWard());
            payload.put("category", g.getCategory());
            payload.put("department", g.getDepartment() == null ? "Unknown" : g.getDepartment());
            payload.put("description_length", g.getDescription() == null ? 0 : g.getDescription().length());
            DayOfWeek dow = g.getDateRaised() != null ? g.getDateRaised().getDayOfWeek() : DayOfWeek.MONDAY;
            payload.put("day_of_week", dow.getValue()); // 1=Monday .. 7=Sunday

            Map<?, ?> result = restClient.post()
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

            if (result == null) return null;

            Object confObj = result.get("confidence");
            Object labelObj = result.get("label");
            if (confObj == null || labelObj == null) return null;

            double confidence = ((Number) confObj).doubleValue();
            if (confidence < CONFIDENCE_THRESHOLD) {
                log.info("Prediction confidence {} below threshold - not shown to user", confidence);
                return null; // Task 5 rule: low confidence -> no forced prediction
            }

            Prediction p = new Prediction();
            p.label = labelObj.toString();
            p.confidence = confidence;
            return p;
        } catch (Exception e) {
            // ML service down/unreachable: the app must still work without it.
            log.warn("ML prediction service unavailable: {}", e.getMessage());
            return null;
        }
    }
}
