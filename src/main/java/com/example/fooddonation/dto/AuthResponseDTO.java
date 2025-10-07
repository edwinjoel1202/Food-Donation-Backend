package com.example.fooddonation.dto;

public class AuthResponseDTO {
    private String token;
    private Long userId;
    private String email;
    private String name;
    private String role;

    // NEW: address/location fields returned after register/login
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Double lat;
    private Double lng;

    public AuthResponseDTO() {}

    public AuthResponseDTO(String token, Long userId, String email, String name, String role) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    // Convenience constructor including address/location
    public AuthResponseDTO(String token, Long userId, String email, String name, String role,
                           String address, Double lat, Double lng, String city, String state, String postalCode, String country) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
    }

    // getters & setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}
