// filename: src/main/java/com/example/fooddonation/controller/RequestController.java
package com.example.fooddonation.controller;

import com.example.fooddonation.dto.DonationRequestDTO;
import com.example.fooddonation.dto.GenericResponseDTO;
import com.example.fooddonation.model.Donation;
import com.example.fooddonation.model.DonationRequest;
import com.example.fooddonation.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller to manage donation requests.
 * - GET /api/requests -> donor/admin view (requests for donations they created)
 * - GET /api/requests/my -> requests created by current user (the "My Requests" view)
 * - POST /api/requests -> create a request
 * - POST /api/requests/{id}/action -> approve/reject (donor)
 * - POST /api/requests/{id}/cancel -> cancel by requester
 */

@RestController
@RequestMapping("/api/requests")
public class RequestController {
    @Autowired
    private RequestService requestService;

    private String currentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody DonationRequestDTO dto) {
        DonationRequest created = requestService.createRequest(dto, currentUserEmail());
        return ResponseEntity.ok(created);
    }

    /**
     * List requests relevant to the current authenticated user.
     * Admins will receive all requests; other users will receive only requests for donations they created.
     */
    @GetMapping
    public ResponseEntity<List<DonationRequest>> listAll() {
        String email = currentUserEmail();
        return ResponseEntity.ok(requestService.listRequestsForUser(email));
    }

    /**
     * "My Requests" endpoint - returns requests created by the current user.
     */
    @GetMapping("/my")
    public ResponseEntity<List<Map<String, Object>>> myRequests() {
        String email = currentUserEmail();
        List<DonationRequest> requests = requestService.listRequestsByRequester(email);
        return ResponseEntity.ok(requests.stream().map(this::toMap).collect(Collectors.toList()));
    }

    @PostMapping("/{id}/action")
    public ResponseEntity<?> action(@PathVariable Long id, @RequestParam("approve") boolean approve) {
        requestService.approveRequest(id, currentUserEmail(), approve);
        return ResponseEntity.ok(new GenericResponseDTO(true, approve ? "Approved" : "Rejected"));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        requestService.cancelRequest(id, currentUserEmail());
        return ResponseEntity.ok(new GenericResponseDTO(true, "Cancelled"));
    }

    private Map<String, Object> toMap(DonationRequest dr) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", dr.getId());
        m.put("status", dr.getStatus());
        m.put("message", dr.getMessage());
        m.put("createdAt", dr.getCreatedAt());
        m.put("actedAt", dr.getActedAt());
        // requester
        if (dr.getRequester() != null) {
            Map<String,Object> req = new HashMap<>();
            req.put("id", dr.getRequester().getId());
            req.put("name", dr.getRequester().getName());
            req.put("email", dr.getRequester().getEmail());
            m.put("requester", req);
        }
        // donation + donor info
        Donation d = dr.getDonation();
        if (d != null) {
            Map<String,Object> dd = new HashMap<>();
            dd.put("id", d.getId());
            dd.put("title", d.getTitle());
            dd.put("description", d.getDescription());
            dd.put("quantity", d.getQuantity());
            dd.put("unit", d.getUnit());
            dd.put("status", d.getStatus());
            if (d.getCreatedBy() != null) {
                Map<String,Object> owner = new HashMap<>();
                owner.put("id", d.getCreatedBy().getId());
                owner.put("name", d.getCreatedBy().getName());
                owner.put("email", d.getCreatedBy().getEmail());
                dd.put("createdBy", owner);
            }
            m.put("donation", dd);
        }
        return m;
    }
}
