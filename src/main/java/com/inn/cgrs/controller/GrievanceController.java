package com.inn.cgrs.controller;

import com.inn.cgrs.model.Grievance;
import com.inn.cgrs.model.GrievanceCategory;
import com.inn.cgrs.model.User;
import com.inn.cgrs.service.FileStorageService;
import com.inn.cgrs.service.GrievanceService;
import com.inn.cgrs.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/grievance")
public class GrievanceController {
    
    @Autowired
    private GrievanceService grievanceService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @GetMapping("/new")
    public String showGrievanceForm(Model model) {
        model.addAttribute("grievance", new Grievance());
        model.addAttribute("categories", GrievanceCategory.values());
        return "grievance-form";
    }
    
    @PostMapping("/submit")
    public String submitGrievance(@Valid @ModelAttribute("grievance") Grievance grievance,
                                  BindingResult result,
                                  @RequestParam(value = "image", required = false) MultipartFile imageFile,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", GrievanceCategory.values());
            return "grievance-form";
        }
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userService.findByUsername(username);
            grievance.setUser(user);
            
            // Handle image upload
            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    String imagePath = fileStorageService.storeFile(imageFile);
                    grievance.setImagePath(imagePath);
                } catch (IOException e) {
                    redirectAttributes.addFlashAttribute("error", "Error uploading image: " + e.getMessage());
                    return "redirect:/grievance/new";
                }
            }
            
            grievanceService.createGrievance(grievance);
            redirectAttributes.addFlashAttribute("success", "Grievance submitted successfully!");
            return "redirect:/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error submitting grievance: " + e.getMessage());
            return "redirect:/grievance/new";
        }
    }
    
    @GetMapping("/view/{id}")
    public String viewGrievance(@PathVariable Long id, Model model) {
        try {
            Grievance grievance = grievanceService.getGrievanceById(id);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userService.findByUsername(username);
            
            // Check if user owns this grievance or is admin
            if (!grievance.getUser().getId().equals(user.getId()) && !auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return "redirect:/dashboard";
            }
            
            model.addAttribute("grievance", grievance);
            return "grievance-details";
        } catch (Exception e) {
            return "redirect:/dashboard";
        }
    }
}
