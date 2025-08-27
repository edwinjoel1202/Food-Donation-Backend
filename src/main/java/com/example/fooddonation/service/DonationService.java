// filename: src/main/java/com/example/fooddonation/service/DonationService.java
package com.example.fooddonation.service;

import com.example.fooddonation.dto.DonationCreateDTO;
import com.example.fooddonation.dto.DonationDTO;
import com.example.fooddonation.exception.ApiException;
import com.example.fooddonation.model.Donation;
import com.example.fooddonation.repository.DonationRepository;
import com.example.fooddonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DonationService {

    @Autowired
    private DonationRepository donationRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private AiService aiService;

    @Transactional
    public DonationDTO createDonation(DonationCreateDTO dto, String creatorEmail) {
        var user = userRepo.findByEmail(creatorEmail).orElseThrow(() -> new ApiException("Creator not found"));
        Donation d = new Donation();
        d.setTitle(dto.getTitle());
        d.setDescription(dto.getDescription());
        // AI auto-categorization fallback: if category missing, call AI
        String category = dto.getCategory();
        if (category == null || category.isEmpty()) {
            category = aiService.categorizeFood(dto.getTitle());
        }
        d.setCategory(category);
        d.setQuantity(dto.getQuantity());
        d.setUnit(dto.getUnit());
        if (dto.getExpiryAt() != null && !dto.getExpiryAt().isEmpty()) {
            try {
                d.setExpiryAt(LocalDateTime.parse(dto.getExpiryAt()));
            } catch (DateTimeParseException e) {
                // ignore or set null
            }
        }
        d.setPickupLat(dto.getPickupLat());
        d.setPickupLng(dto.getPickupLng());
        d.setCreatedBy(user);
        if (dto.getImageBase64() != null && !dto.getImageBase64().isEmpty()) {
            String url = cloudinaryService.uploadBase64(dto.getImageBase64(), "donations");
            d.setImageUrl(url);
        }
        Donation saved = donationRepo.save(d);
        return toDto(saved);
    }

    public DonationDTO toDto(Donation d) {
        DonationDTO dto = new DonationDTO();
        dto.setId(d.getId());
        dto.setTitle(d.getTitle());
        dto.setDescription(d.getDescription());
        dto.setCategory(d.getCategory());
        dto.setQuantity(d.getQuantity());
        dto.setUnit(d.getUnit());
        dto.setExpiryAt(d.getExpiryAt() != null ? d.getExpiryAt().toString() : null);
        dto.setPickupLat(d.getPickupLat());
        dto.setPickupLng(d.getPickupLng());
        dto.setImageUrl(d.getImageUrl());
        dto.setStatus(d.getStatus());
        if (d.getCreatedBy() != null) {
            dto.setCreatedById(d.getCreatedBy().getId());
            dto.setCreatedByName(d.getCreatedBy().getName());
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public List<DonationDTO> listAllAvailable(String currentUserEmail) {
        // fetch all donations with AVAILABLE status, then filter out those
        // created by the calling user (so user won't see their own donations in "available")
        return donationRepo.findByStatus("AVAILABLE").stream()
                .filter(d -> {
                    if (d.getCreatedBy() == null) return true;
                    // compare emails to be safe (createdBy is a User entity)
                    String creatorEmail = d.getCreatedBy().getEmail();
                    return creatorEmail == null || !creatorEmail.equals(currentUserEmail);
                })
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DonationDTO getDonation(Long id) {
        Donation d = donationRepo.findById(id).orElseThrow(() -> new ApiException("Donation not found"));
        return toDto(d);
    }

    @Transactional
    public void cancelDonation(Long id, String actorEmail) {
        Donation d = donationRepo.findById(id).orElseThrow(() -> new ApiException("Donation not found"));
        if (!d.getCreatedBy().getEmail().equals(actorEmail)) {
            throw new ApiException("Only creator can cancel");
        }
        d.setStatus("CANCELLED");
        donationRepo.save(d);
    }

    @Transactional(readOnly = true)
    public List<DonationDTO> myDonations(String userEmail) {
        var user = userRepo.findByEmail(userEmail).orElseThrow(() -> new ApiException("User not found"));
        return donationRepo.findByCreatedById(user.getId()).stream().map(this::toDto).collect(Collectors.toList());
    }

    // other update methods (approve/fulfill) can be added by admin/request workflow
}