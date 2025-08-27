// filename: src/main/java/com/example/fooddonation/controller/DonationController.java
package com.example.fooddonation.controller;

import com.example.fooddonation.dto.*;
import com.example.fooddonation.service.DonationService;
import com.example.fooddonation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donations")
public class DonationController {

    @Autowired
    private DonationService donationService;

    @Autowired
    private UserService userService;

    private String currentUserEmail() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    @PostMapping
    public ResponseEntity<DonationDTO> createDonation(@RequestBody DonationCreateDTO dto) {
        DonationDTO created = donationService.createDonation(dto, currentUserEmail());
        return ResponseEntity.ok(created);
    }

    @GetMapping("/available")
    public ResponseEntity<List<DonationDTO>> listAvailable() {
        return ResponseEntity.ok(donationService.listAllAvailable(currentUserEmail()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DonationDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(donationService.getDonation(id));
    }

    @GetMapping("/my")
    public ResponseEntity<List<DonationDTO>> myDonations() {
        return ResponseEntity.ok(donationService.myDonations(currentUserEmail()));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<GenericResponseDTO> cancel(@PathVariable Long id) {
        donationService.cancelDonation(id, currentUserEmail());
        return ResponseEntity.ok(new GenericResponseDTO(true, "Cancelled"));
    }
}