// filename: src/main/java/com/example/fooddonation/service/RequestService.java
package com.example.fooddonation.service;

import com.example.fooddonation.dto.DonationRequestDTO;
import com.example.fooddonation.exception.ApiException;
import com.example.fooddonation.model.Donation;
import com.example.fooddonation.model.DonationRequest;
import com.example.fooddonation.model.Role;
import com.example.fooddonation.repository.DonationRepository;
import com.example.fooddonation.repository.DonationRequestRepository;
import com.example.fooddonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RequestService {

    @Autowired
    private DonationRepository donationRepo;
    @Autowired
    private DonationRequestRepository requestRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private EmailService emailService;

    @Transactional
    public DonationRequest createRequest(DonationRequestDTO dto, String requesterEmail) {
        var requester = userRepo.findByEmail(requesterEmail).orElseThrow(() -> new ApiException("Requester not found"));
        var donation = donationRepo.findById(dto.getDonationId()).orElseThrow(() -> new ApiException("Donation not found"));

        if (!"AVAILABLE".equals(donation.getStatus())) throw new ApiException("Donation not available");

        // prevent the donation creator from requesting their own donation
        if (donation.getCreatedBy() != null && requester.getEmail().equals(donation.getCreatedBy().getEmail())) {
            throw new ApiException("You cannot request your own donation");
        }

        DonationRequest dr = new DonationRequest();
        dr.setDonation(donation);
        dr.setRequester(requester);
        dr.setMessage(dto.getMessage());
        dr.setStatus("PENDING");
        dr.setCreatedAt(LocalDateTime.now());

        DonationRequest saved = requestRepo.save(dr);

        // notify donor (if email present)
        if (donation.getCreatedBy() != null && donation.getCreatedBy().getEmail() != null) {
            try {
                emailService.sendSimpleMail(donation.getCreatedBy().getEmail(),
                        "New request for your donation",
                        "Someone requested your donation: " + donation.getTitle() + "\n\nMessage: " + dto.getMessage());
            } catch (Exception ignored) { }
        }

        return saved;
    }

    /**
     * NEW: Return requests relevant to the current authenticated user.
     * - If user is ADMIN -> return all requests
     * - Otherwise -> only requests where the donation was created by this user
     */
    @Transactional(readOnly = true)
    public List<DonationRequest> listRequestsForUser(String currentUserEmail) {
        var user = userRepo.findByEmail(currentUserEmail).orElseThrow(() -> new ApiException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            // admin sees all requests
            return requestRepo.findAll();
        }

        // donor: return requests for donations the user created
        return requestRepo.findByDonationCreatedById(user.getId());
    }

    // Keep original method if you still need it elsewhere (optional)
    @Transactional(readOnly = true)
    public List<DonationRequest> listAllRequests() {
        return requestRepo.findAll();
    }

    @Transactional
    public void approveRequest(Long requestId, String approverEmail, boolean approve) {
        DonationRequest dr = requestRepo.findById(requestId).orElseThrow(() -> new ApiException("Request not found"));
        Donation donation = dr.getDonation();
        if (donation == null) throw new ApiException("Donation not found");

        if (donation.getCreatedBy() == null || !donation.getCreatedBy().getEmail().equals(approverEmail)) {
            throw new ApiException("Only donation owner can approve/reject");
        }

        if (approve) {
            dr.setStatus("APPROVED");
            donation.setStatus("REQUESTED");
        } else {
            dr.setStatus("REJECTED");
            donation.setStatus("AVAILABLE");
        }
        dr.setActedAt(LocalDateTime.now());
        requestRepo.save(dr);
        donationRepo.save(donation);

        // notify requester
        try {
            emailService.sendSimpleMail(
                    dr.getRequester().getEmail(),
                    "Your donation request has been " + (approve ? "approved" : "rejected"),
                    "Request for donation " + donation.getTitle() + " has been " + (approve ? "approved" : "rejected") + "."
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
