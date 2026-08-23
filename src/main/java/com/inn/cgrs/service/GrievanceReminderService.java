package com.inn.cgrs.service;

import com.inn.cgrs.model.Grievance;
import com.inn.cgrs.model.GrievanceStatus;
import com.inn.cgrs.model.Role;
import com.inn.cgrs.model.User;
import com.inn.cgrs.repository.GrievanceRepository;
import com.inn.cgrs.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class GrievanceReminderService {

    private static final List<GrievanceStatus> UNRESOLVED_STATUSES = Arrays.asList(
            GrievanceStatus.PENDING,
            GrievanceStatus.IN_PROGRESS
    );

    @Autowired
    private GrievanceRepository grievanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Runs daily at 9:00 AM. Finds grievances unresolved for more than 1 week
     * and sends a reminder email to all admins.
     */
    @Scheduled(cron = "${cgrs.reminder.cron:0 0 9 * * ?}")
    public void sendUnresolvedGrievanceReminders() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        List<Grievance> unresolved = grievanceRepository
                .findByStatusInAndCreatedAtBeforeOrderByCreatedAtAsc(UNRESOLVED_STATUSES, oneWeekAgo);

        if (unresolved.isEmpty()) {
            return;
        }

        List<User> admins = userRepository.findByRole(Role.ADMIN);
        if (admins.isEmpty()) {
            return;
        }

        for (User admin : admins) {
            if (admin.getEmail() != null && !admin.getEmail().isBlank()) {
                try {
                    emailService.sendUnresolvedGrievanceReminderToAdmin(admin.getEmail(), unresolved);
                } catch (Exception e) {
                    // Log but don't fail the job; other admins may still receive the email
                    e.printStackTrace();
                    System.err.println("Failed to send reminder to admin " + admin.getEmail() + ": " + e.getMessage());
                }
            }
        }
    }
}
