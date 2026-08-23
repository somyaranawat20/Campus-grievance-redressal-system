package com.inn.cgrs.controller;

import com.inn.cgrs.model.Grievance;
import com.inn.cgrs.model.GrievanceCategory;
import com.inn.cgrs.model.GrievanceStatus;
import com.inn.cgrs.service.GrievanceService;
import com.inn.cgrs.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private GrievanceService grievanceService;

    @Autowired
    private ReportService reportService;
    
    @GetMapping("/dashboard")
    public String adminDashboard(
            @RequestParam(required = false) GrievanceStatus status,
            @RequestParam(required = false) String time,
            Model model) {
        List<Grievance> grievances = grievanceService.getGrievancesForDashboard(status, time);
        long pendingCount = grievanceService.countByStatusForPeriod(GrievanceStatus.PENDING, time);
        long inProgressCount = grievanceService.countByStatusForPeriod(GrievanceStatus.IN_PROGRESS, time);
        long resolvedCount = grievanceService.countByStatusForPeriod(GrievanceStatus.RESOLVED, time);
        long rejectedCount = grievanceService.countByStatusForPeriod(GrievanceStatus.REJECTED, time);

        model.addAttribute("grievances", grievances);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedTime", time != null ? time.toUpperCase() : null);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("resolvedCount", resolvedCount);
        model.addAttribute("rejectedCount", rejectedCount);

        return "admin-dashboard";
    }
    
    @GetMapping("/grievance/{id}")
    public String viewGrievance(@PathVariable Long id, Model model) {
        try {
            Grievance grievance = grievanceService.getGrievanceById(id);
            model.addAttribute("grievance", grievance);
            model.addAttribute("statuses", GrievanceStatus.values());
            return "admin-grievance-details";
        } catch (Exception e) {
            return "redirect:/admin/dashboard";
        }
    }
    
    @PostMapping("/grievance/{id}/update")
    public String updateGrievanceStatus(@PathVariable Long id,
                                       @RequestParam GrievanceStatus status,
                                       @RequestParam(required = false) String remarks,
                                       RedirectAttributes redirectAttributes) {
        try {
            grievanceService.updateGrievanceStatus(id, status, remarks);
            redirectAttributes.addFlashAttribute("success", "Grievance status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating grievance: " + e.getMessage());
        }
        return "redirect:/admin/grievance/" + id;
    }
    
    @GetMapping("/reports")
    public String reports(Model model) {
        // Overall statistics
        long totalGrievances = grievanceService.getAllGrievances().size();
        long pendingCount = grievanceService.countByStatus(GrievanceStatus.PENDING);
        long inProgressCount = grievanceService.countByStatus(GrievanceStatus.IN_PROGRESS);
        long resolvedCount = grievanceService.countByStatus(GrievanceStatus.RESOLVED);
        long rejectedCount = grievanceService.countByStatus(GrievanceStatus.REJECTED);
        
        // Category-wise statistics
        Map<GrievanceCategory, Long> categoryStats = new HashMap<>();
        for (GrievanceCategory category : GrievanceCategory.values()) {
            long count = grievanceService.getAllGrievances().stream()
                    .filter(g -> g.getCategory() == category)
                    .count();
            categoryStats.put(category, count);
        }
        
        // Recent grievances (last 10)
        List<Grievance> recentGrievances = grievanceService.getAllGrievances().stream()
                .limit(10)
                .toList();
        
        model.addAttribute("totalGrievances", totalGrievances);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("resolvedCount", resolvedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("categoryStats", categoryStats);
        model.addAttribute("recentGrievances", recentGrievances);
        model.addAttribute("categories", GrievanceCategory.values());
        
        // Calculate resolution rate
        double resolutionRate = totalGrievances > 0 
            ? (double) resolvedCount / totalGrievances * 100 
            : 0.0;
        model.addAttribute("resolutionRate", String.format("%.2f", resolutionRate));
        
        return "admin-reports";
    }

    /**
     * Download grievances report as CSV for the given period (WEEK, MONTH, YEAR).
     * Report includes all grievances in that period with their current status.
     */
    @GetMapping("/reports/download")
    public ResponseEntity<byte[]> downloadReport(@RequestParam(name = "period") String period) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start;
        String periodLabel;
        switch (period.toUpperCase()) {
            case "WEEK":
                start = end.minusWeeks(1);
                periodLabel = "week";
                break;
            case "MONTH":
                start = end.minusMonths(1);
                periodLabel = "month";
                break;
            case "YEAR":
                start = end.minusYears(1);
                periodLabel = "year";
                break;
            default:
                return ResponseEntity.badRequest().build();
        }

        List<Grievance> grievances = grievanceService.getGrievancesByDateRange(start, end);
        byte[] csv = reportService.generateGrievancesCsv(grievances);

        String filename = "grievances_report_" + periodLabel + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".csv";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(csv);
    }
}
