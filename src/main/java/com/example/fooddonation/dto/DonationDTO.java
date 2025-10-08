// src/main/java/com/example/fooddonation/dto/DonationDTO.java
package com.example.fooddonation.dto;

public class DonationDTO {
    private Long id;
    private String title;
    private String description;
    private String category;
    private Double quantity;
    private String unit;
    private String expiryAt;
    private Double pickupLat;
    private Double pickupLng;

    // NEW: pickup address fields in response
    private String pickupAddress;
    private String pickupCity;
    private String pickupState;
    private String pickupPostalCode;
    private String pickupCountry;

    private String imageUrl;
    private String status;
    private Long createdById;
    private String createdByName;

    // getters/setters for all fields
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
    public String getExpiryAt() { return expiryAt; }
    public void setExpiryAt(String expiryAt) { this.expiryAt = expiryAt; }
    public Double getPickupLat() { return pickupLat; }
    public void setPickupLat(Double pickupLat) { this.pickupLat = pickupLat; }
    public Double getPickupLng() { return pickupLng; }
    public void setPickupLng(Double pickupLng) { this.pickupLng = pickupLng; }

    // pickup address getters/setters
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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }
    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
}
