// filename: src/main/java/com/example/fooddonation/model/DonationRequest.java
package com.example.fooddonation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "donation_requests")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DonationRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Donation donation;

    @ManyToOne
    private User requester;

    @Column(nullable=false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, CANCELLED, FULFILLED

    private String message;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime actedAt;

    // getters/setters
    public DonationRequest() {}

    // getters & setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Donation getDonation() { return donation; }
    public void setDonation(Donation donation) { this.donation = donation; }
    public User getRequester() { return requester; }
    public void setRequester(User requester) { this.requester = requester; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getActedAt() { return actedAt; }
    public void setActedAt(LocalDateTime actedAt) { this.actedAt = actedAt; }
}