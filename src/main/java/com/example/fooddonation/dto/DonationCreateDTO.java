// filename: src/main/java/com/example/fooddonation/dto/DonationCreateDTO.java
package com.example.fooddonation.dto;

public class DonationCreateDTO {
    private String title;
    private String description;
    private String category;
    private Double quantity;
    private String unit;
    private String expiryAt; // ISO string, optional
    private Double pickupLat;
    private Double pickupLng;
    private String imageBase64; // optional - if sending base64 (or we can send multipart)

    // getters/setters
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
    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
}