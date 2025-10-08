// src/main/java/com/example/fooddonation/dto/DonationCreateDTO.java
package com.example.fooddonation.dto;

public class DonationCreateDTO {
    private String title;
    private String description;
    private String category;
    private Double quantity;
    private String unit;
    private String expiryAt;
    private Double pickupLat;
    private Double pickupLng;
    private String pickupAddress;      // NEW
    private String pickupCity;         // NEW
    private String pickupState;        // NEW
    private String pickupPostalCode;   // NEW
    private String pickupCountry;      // NEW
    private String imageBase64;

    // getters/setters for all fields
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

    // NEW fields
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

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
}
