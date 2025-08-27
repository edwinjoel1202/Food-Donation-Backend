// filename: src/main/java/com/example/fooddonation/controller/RequestController.java
package com.example.fooddonation.controller;

import com.example.fooddonation.dto.DonationRequestDTO;
import com.example.fooddonation.dto.GenericResponseDTO;
import com.example.fooddonation.model.DonationRequest;
import com.example.fooddonation.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/{id}/action")
    public ResponseEntity<?> action(@PathVariable Long id, @RequestParam("approve") boolean approve) {
        requestService.approveRequest(id, currentUserEmail(), approve);
        return ResponseEntity.ok(new GenericResponseDTO(true, approve ? "Approved" : "Rejected"));
    }
}
