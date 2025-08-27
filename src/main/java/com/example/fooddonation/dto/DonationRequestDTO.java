// filename: src/main/java/com/example/fooddonation/dto/DonationRequestDTO.java
package com.example.fooddonation.dto;

public class DonationRequestDTO {
    private Long donationId;
    private String message;

    // getters & setters
    public Long getDonationId() { return donationId; }
    public void setDonationId(Long donationId) { this.donationId = donationId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}