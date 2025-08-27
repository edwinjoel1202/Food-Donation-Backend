// filename: src/main/java/com/example/fooddonation/service/RequestService.java
package com.example.fooddonation.service;

import com.example.fooddonation.dto.DonationRequestDTO;
import com.example.fooddonation.exception.ApiException;
import com.example.fooddonation.model.Donation;
import com.example.fooddonation.model.DonationRequest;
import com.example.fooddonation.repository.DonationRepository;
import com.example.fooddonation.repository.DonationRequestRepository;
import com.example.fooddonation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

        DonationRequest dr = new DonationRequest();
        dr.setDonation(donation);
        dr.setRequester(requester);
        dr.setMessage(dto.getMessage());
        dr.setStatus("PENDING");
        dr.setCreatedAt(LocalDateTime.now());

        DonationRequest saved = requestRepo.save(dr);

        // set donation status to REQUESTED (so others know)
        donation.setStatus("REQUESTED");
        donationRepo.save(donation);

        // send email to donation owner
        try {
            emailService.sendSimpleMail(
                    donation.getCreatedBy().getEmail(),
                    "New donation request",
                    "You have a new request from " + requester.getName() + " for donation: " + donation.getTitle()
            );
        } catch (Exception e) {
            // log but don't fail
            e.printStackTrace();
        }

        return saved;
    }

    @Transactional
    public void approveRequest(Long requestId, String approverEmail, boolean approve) {
        DonationRequest dr = requestRepo.findById(requestId).orElseThrow(() -> new ApiException("Request not found"));
        Donation donation = dr.getDonation();
        if (donation == null) throw new ApiException("Donation not found");

        if (!donation.getCreatedBy().getEmail().equals(approverEmail)) {
            throw new ApiException("Only donation owner can approve/reject");
        }

        if (approve) {
            dr.setStatus("APPROVED");
            donation.setStatus("REQUESTED"); // still requested until delivery / fulfilled
            // optionally set volunteer assignment here
        } else {
            dr.setStatus("REJECTED");
            donation.setStatus("AVAILABLE");
        }
        dr.setActedAt(LocalDateTime.now());
        requestRepo.save(dr);
        donationRepo.save(donation);

        // send email to requester
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