package com.inn.cgrs.service;

import com.inn.cgrs.model.Grievance;
import com.inn.cgrs.model.GrievanceStatus;
import com.inn.cgrs.model.User;
import com.inn.cgrs.repository.GrievanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class GrievanceService {
    
    @Autowired
    private GrievanceRepository grievanceRepository;
    
    @Autowired
    private EmailService emailService;
    
    public Grievance createGrievance(Grievance grievance) {
        Grievance savedGrievance = grievanceRepository.save(grievance);
        emailService.sendGrievanceSubmitted(
            grievance.getUser().getEmail(),
            grievance.getSubject(),
            savedGrievance.getId()
        );
        return savedGrievance;
    }
    
    public List<Grievance> getAllGrievances() {
        return grievanceRepository.findAllByOrderByCreatedAtDesc();
    }
    
    public List<Grievance> getGrievancesByUser(User user) {
        return grievanceRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    public Grievance getGrievanceById(Long id) {
        return grievanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grievance not found"));
    }
    
    public Grievance updateGrievanceStatus(Long id, GrievanceStatus status, String remarks) {
        Grievance grievance = getGrievanceById(id);
        grievance.setStatus(status);
        if (remarks != null && !remarks.trim().isEmpty()) {
            grievance.setAdminRemarks(remarks);
        }
        
        Grievance updatedGrievance = grievanceRepository.save(grievance);
        
        // Send email notification
        emailService.sendGrievanceStatusUpdate(
            grievance.getUser().getEmail(),
            grievance.getSubject(),
            status.getDisplayName(),
            remarks
        );
        
        return updatedGrievance;
    }
    
    public List<Grievance> getGrievancesByStatus(GrievanceStatus status) {
        return grievanceRepository.findByStatus(status);
    }
    
    public long countByStatus(GrievanceStatus status) {
        return grievanceRepository.countByStatus(status);
    }
    
    public void deleteGrievance(Long id) {
        grievanceRepository.deleteById(id);
    }

    public List<Grievance> getGrievancesByDateRange(LocalDateTime start, LocalDateTime end) {
        return grievanceRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
    }

    /**
     * Get grievances for dashboard with optional status and time period filter.
     * timePeriod: DAILY, WEEKLY, MONTHLY, YEARLY or null for all time.
     */
    public List<Grievance> getGrievancesForDashboard(GrievanceStatus status, String timePeriod) {
        LocalDateTime[] range = getDateRangeForPeriod(timePeriod);
        if (range == null) {
            return status == null ? grievanceRepository.findAllByOrderByCreatedAtDesc()
                    : grievanceRepository.findByStatus(status);
        }
        LocalDateTime start = range[0];
        LocalDateTime end = range[1];
        if (status == null) {
            return grievanceRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
        }
        return grievanceRepository.findByStatusAndCreatedAtBetweenOrderByCreatedAtDesc(status, start, end);
    }

    /**
     * Get counts by status for the given time period. If timePeriod is null, returns global counts.
     */
    public long countByStatusForPeriod(GrievanceStatus status, String timePeriod) {
        LocalDateTime[] range = getDateRangeForPeriod(timePeriod);
        if (range == null) {
            return grievanceRepository.countByStatus(status);
        }
        return grievanceRepository.countByStatusAndCreatedAtBetween(status, range[0], range[1]);
    }

    /**
     * Returns [start, end] for the period, or null if period is null/empty/invalid.
     * DAILY = from start of today; WEEKLY = last 7 days; MONTHLY = last 30 days; YEARLY = last 365 days.
     */
    public static LocalDateTime[] getDateRangeForPeriod(String timePeriod) {
        if (timePeriod == null || timePeriod.isBlank()) {
            return null;
        }
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start;
        switch (timePeriod.toUpperCase()) {
            case "DAILY":
                start = LocalDate.now().atStartOfDay();
                break;
            case "WEEKLY":
                start = end.minusWeeks(1);
                break;
            case "MONTHLY":
                start = end.minusMonths(1);
                break;
            case "YEARLY":
                start = end.minusYears(1);
                break;
            default:
                return null;
        }
        return new LocalDateTime[]{start, end};
    }
}
