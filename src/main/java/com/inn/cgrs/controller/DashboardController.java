package com.inn.cgrs.controller;

import com.inn.cgrs.model.Grievance;
import com.inn.cgrs.model.GrievanceStatus;
import com.inn.cgrs.model.User;
import com.inn.cgrs.service.GrievanceService;
import com.inn.cgrs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DashboardController {
    
    @Autowired
    private GrievanceService grievanceService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) GrievanceStatus status,
            @RequestParam(required = false) String time,
            Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            // Redirect admins to admin dashboard
            if (auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return "redirect:/admin/dashboard";
            }
            
            String username = auth.getName();
            User user = userService.findByUsername(username);
            List<Grievance> allMine = grievanceService.getGrievancesByUser(user);
            
            LocalDateTime[] range = GrievanceService.getDateRangeForPeriod(time);
            List<Grievance> periodGrievances = range == null ? allMine : allMine.stream()
                    .filter(g -> g.getCreatedAt() != null && !g.getCreatedAt().isBefore(range[0]) && !g.getCreatedAt().isAfter(range[1]))
                    .collect(Collectors.toList());
            
            List<Grievance> grievances = status == null ? periodGrievances
                    : periodGrievances.stream().filter(g -> g.getStatus() == status).collect(Collectors.toList());
            
            long pendingCount = periodGrievances.stream().filter(g -> g.getStatus() == GrievanceStatus.PENDING).count();
            long inProgressCount = periodGrievances.stream().filter(g -> g.getStatus() == GrievanceStatus.IN_PROGRESS).count();
            long resolvedCount = periodGrievances.stream().filter(g -> g.getStatus() == GrievanceStatus.RESOLVED).count();
            long rejectedCount = periodGrievances.stream().filter(g -> g.getStatus() == GrievanceStatus.REJECTED).count();
            
            model.addAttribute("user", user);
            model.addAttribute("grievances", grievances);
            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("inProgressCount", inProgressCount);
            model.addAttribute("resolvedCount", resolvedCount);
            model.addAttribute("rejectedCount", rejectedCount);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("selectedTime", time != null ? time.toUpperCase() : null);
            
            return "dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "dashboard";
        }
    }
}
