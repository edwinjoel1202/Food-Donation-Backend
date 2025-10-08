// src/main/java/com/example/fooddonation/model/Donation.java
package com.example.fooddonation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "donations")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Donation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String title;

    @Column(length=2000)
    private String description;

    @Column(nullable=false)
    private String category;

    @Column(nullable=false)
    private Double quantity;

    private String unit;

    private LocalDateTime expiryAt;

    private Double pickupLat;
    private Double pickupLng;

    // NEW: pickup address fields
    @Column(length = 1000)
    private String pickupAddress;
    private String pickupCity;
    private String pickupState;
    private String pickupPostalCode;
    private String pickupCountry;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    private User createdBy;

    @Column(nullable=false)
    private String status = "AVAILABLE";

    private LocalDateTime createdAt = LocalDateTime.now();

    public Donation() {}

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public LocalDateTime getExpiryAt() { return expiryAt; }
    public void setExpiryAt(LocalDateTime expiryAt) { this.expiryAt = expiryAt; }

    public Double getPickupLat() { return pickupLat; }
    public void setPickupLat(Double pickupLat) { this.pickupLat = pickupLat; }
    public Double getPickupLng() { return pickupLng; }
    public void setPickupLng(Double pickupLng) { this.pickupLng = pickupLng; }

    // NEW pickup address getters/setters
    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }
    public String getPickupCity() { return pickupCity; }
    public void setPickupCity(String pickupCity) { this.pickupCity = pickupCity; }
    public String getPickupState() { return pickupState; }
    public void setPickupState(String pickupState) { this.pickupState = pickupState; }
    public String getPickupPostalCode() { return pickupPostalCode; }
    public void setPickupPostalCode(String pickupPostalCode) { this.pickupPostalCode = pickupPostalCode; }
    public String getPickupCountry() { return pickupCountry; }
    public void setPickupCountry(String pickupCountry) { this.pickupCountry = pickupCountry; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
