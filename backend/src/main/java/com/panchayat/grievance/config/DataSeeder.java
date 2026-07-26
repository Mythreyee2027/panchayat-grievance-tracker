package com.panchayat.grievance.config;

import com.panchayat.grievance.model.Grievance;
import com.panchayat.grievance.repository.GrievanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads data/grievances.csv (Task 1's dataset) into the database once,
 * the first time the app starts against an empty table.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final GrievanceRepository repository;
    private final String csvPath;

    public DataSeeder(GrievanceRepository repository,
                       @Value("${app.seed-csv-path}") String csvPath) {
        this.repository = repository;
        this.csvPath = csvPath;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            log.info("Grievances table already has data - skipping CSV seed.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath))) {
            String header = reader.readLine(); // skip header row
            String line;
            List<Grievance> batch = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                List<String> f = parseCsvLine(line);
                if (f.size() < 11) continue;

                Grievance g = new Grievance();
                g.setGrievanceId(f.get(0));
                g.setComplainant(f.get(1));
                g.setWard(parseIntOrNull(f.get(2)));
                g.setCategory(f.get(3));
                g.setDepartment(f.get(4).isBlank() ? null : f.get(4));
                g.setDescription(f.get(5));
                g.setDateRaised(parseDateOrNull(f.get(6)));
                g.setStatus(f.get(7));
                g.setResolvedDate(parseDateOrNull(f.get(8)));
                g.setDaysToResolve(parseIntOrNull(f.get(9)));
                g.setDelayed(parseIntOrNull(f.get(10)));

                batch.add(g);
            }
            repository.saveAll(batch);
            log.info("Seeded {} grievances from {}", batch.size(), csvPath);
        } catch (Exception e) {
            log.warn("Could not seed from CSV at {} ({}). Starting with an empty table - " +
                    "you can still add grievances through the app.", csvPath, e.getMessage());
        }
    }

    // Minimal CSV line parser that handles quoted fields containing commas
    // (needed because some descriptions in the dataset contain commas, e.g. "Drain blocked, water stagnant").
    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        result.add(cur.toString());
        return result;
    }

    private Integer parseIntOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private LocalDate parseDateOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s.trim()); } catch (Exception e) { return null; }
    }
}
