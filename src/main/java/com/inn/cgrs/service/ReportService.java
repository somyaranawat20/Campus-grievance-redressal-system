package com.inn.cgrs.service;

import com.inn.cgrs.model.Grievance;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Generates CSV content for grievances report with current status and all details.
     */
    public byte[] generateGrievancesCsv(List<Grievance> grievances) {
        StringBuilder csv = new StringBuilder();
        // Header row
        csv.append(escapeCsv("ID")).append(",")
           .append(escapeCsv("Subject")).append(",")
           .append(escapeCsv("Description")).append(",")
           .append(escapeCsv("Category")).append(",")
           .append(escapeCsv("Location")).append(",")
           .append(escapeCsv("Current Status")).append(",")
           .append(escapeCsv("Submitted By")).append(",")
           .append(escapeCsv("Email")).append(",")
           .append(escapeCsv("Created At")).append(",")
           .append(escapeCsv("Updated At")).append(",")
           .append(escapeCsv("Admin Remarks")).append("\n");

        for (Grievance g : grievances) {
            csv.append(escapeCsv(g.getId().toString())).append(",")
               .append(escapeCsv(g.getSubject())).append(",")
               .append(escapeCsv(g.getDescription())).append(",")
               .append(escapeCsv(g.getCategory() != null ? g.getCategory().getDisplayName() : "")).append(",")
               .append(escapeCsv(g.getLocation())).append(",")
               .append(escapeCsv(g.getStatus() != null ? g.getStatus().getDisplayName() : "")).append(",")
               .append(escapeCsv(g.getUser() != null ? g.getUser().getFullName() : "")).append(",")
               .append(escapeCsv(g.getUser() != null ? g.getUser().getEmail() : "")).append(",")
               .append(escapeCsv(g.getCreatedAt() != null ? g.getCreatedAt().format(DATE_FORMAT) : "")).append(",")
               .append(escapeCsv(g.getUpdatedAt() != null ? g.getUpdatedAt().format(DATE_FORMAT) : "")).append(",")
               .append(escapeCsv(g.getAdminRemarks())).append("\n");
        }

        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "\"\"";
        }
        String s = value.replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s + "\"";
        }
        return s;
    }
}
