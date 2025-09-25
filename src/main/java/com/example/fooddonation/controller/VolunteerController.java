package com.example.fooddonation.controller;

import com.example.fooddonation.dto.GenericResponseDTO;
import com.example.fooddonation.exception.ApiException;
import com.example.fooddonation.model.Donation;
import com.example.fooddonation.model.Role;
import com.example.fooddonation.model.User;
import com.example.fooddonation.repository.DonationRepository;
import com.example.fooddonation.repository.UserRepository;
import com.example.fooddonation.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Volunteer endpoints.
 */
@RestController
@RequestMapping("/api/volunteer")
public class VolunteerController {

    @Autowired
    private DonationRepository donationRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private EmailService emailService;

    private String currentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @PostMapping("/accept/{donationId}")
    public ResponseEntity<GenericResponseDTO> accept(@PathVariable Long donationId) {
        String volunteerEmail = currentUserEmail();
        User volunteer = userRepo.findByEmail(volunteerEmail).orElseThrow(() -> new ApiException("Volunteer user not found"));

        if (volunteer.getRole() != Role.VOLUNTEER) {
            throw new ApiException("Only users with VOLUNTEER role can accept deliveries");
        }

        Donation d = donationRepo.findById(donationId).orElseThrow(() -> new ApiException("Donation not found"));

        // mark donation in transit (a minimal approach without extra DB tables)
        d.setStatus("IN_TRANSIT");
        donationRepo.save(d);

        // Notify donor (if email available)
        try {
            if (d.getCreatedBy() != null && d.getCreatedBy().getEmail() != null) {
                String subject = "Volunteer assigned to your donation";
                String text = "Hello " + (d.getCreatedBy().getName() == null ? "" : d.getCreatedBy().getName()) + ",\n\n"
                        + "Volunteer " + volunteer.getName() + " (" + volunteer.getEmail() + ") has accepted delivery for your donation: " + d.getTitle()
                        + ".\nPlease coordinate pickup/drop details as needed.\n\nThanks,\nFood Donation Team";
                emailService.sendSimpleMail(d.getCreatedBy().getEmail(), subject, text);
            }
        } catch (Exception e) {
            // ignore email failure
        }

        return ResponseEntity.ok(new GenericResponseDTO(true, "Volunteer accepted and donor notified."));
    }
}