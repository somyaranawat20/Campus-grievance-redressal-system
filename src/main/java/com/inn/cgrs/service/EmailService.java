package com.inn.cgrs.service;

import com.inn.cgrs.model.Grievance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    public void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            // Log error but don't throw exception to avoid breaking the flow
        }
    }
    
    public void sendGrievanceStatusUpdate(String userEmail, String grievanceSubject, String status, String remarks) {
        String subject = "Grievance Status Update - " + grievanceSubject;
        String text = String.format(
            "Dear User,\n\n" +
            "Your grievance regarding '%s' has been updated.\n\n" +
            "Status: %s\n" +
            "%s\n\n" +
            "Thank you for using Campus Grievance Redressal System.\n\n" +
            "Best Regards,\n" +
            "CGRS Team",
            grievanceSubject,
            status,
            remarks != null && !remarks.isEmpty() ? "Remarks: " + remarks : ""
        );
        sendEmail(userEmail, subject, text);
    }
    
    public void sendGrievanceSubmitted(String userEmail, String grievanceSubject, Long grievanceId) {
        String subject = "Grievance Submitted Successfully";
        String text = String.format(
            "Dear User,\n\n" +
            "Your grievance has been submitted successfully.\n\n" +
            "Subject: %s\n" +
            "Grievance ID: #%d\n" +
            "Status: Pending\n\n" +
            "We will review your complaint and update you soon.\n\n" +
            "Thank you for using Campus Grievance Redressal System.\n\n" +
            "Best Regards,\n" +
            "CGRS Team",
            grievanceSubject,
            grievanceId
        );
        sendEmail(userEmail, subject, text);
    }

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    /**
     * Sends a reminder to an admin about grievances unresolved for more than 1 week.
     */
    public void sendUnresolvedGrievanceReminderToAdmin(String adminEmail, List<Grievance> unresolvedGrievances) {
        if (unresolvedGrievances == null || unresolvedGrievances.isEmpty()) {
            return;
        }
        String subject = "CGRS Reminder: " + unresolvedGrievances.size() + " Grievance(s) Unresolved for Over 1 Week";
        StringBuilder text = new StringBuilder();
        text.append("Dear Admin,\n\n");
        text.append("The following grievance(s) have been pending for more than 1 week and require attention:\n\n");
        for (Grievance g : unresolvedGrievances) {
            text.append("---\n");
            text.append("ID: #").append(g.getId()).append("\n");
            text.append("Subject: ").append(g.getSubject()).append("\n");
            text.append("Category: ").append(g.getCategory() != null ? g.getCategory().getDisplayName() : "").append("\n");
            text.append("Location: ").append(g.getLocation()).append("\n");
            text.append("Status: ").append(g.getStatus() != null ? g.getStatus().getDisplayName() : "").append("\n");
            text.append("Submitted: ").append(g.getCreatedAt() != null ? g.getCreatedAt().format(DATE_FORMAT) : "").append("\n");
            text.append("Submitted by: ").append(g.getUser() != null ? g.getUser().getFullName() + " (" + g.getUser().getEmail() + ")" : "").append("\n\n");
        }
        text.append("---\n\n");
        text.append("Please log in to the admin dashboard to resolve these grievances.\n\n");
        text.append("Best Regards,\nCGRS System");
        sendEmail(adminEmail, subject, text.toString());
    }
}
