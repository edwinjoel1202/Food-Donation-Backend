package com.example.fooddonation.dto;

import com.example.fooddonation.model.Role;

public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private Role role;

    // address/location fields
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Double lat;
    private Double lng;

    public UserDTO() {}

    public UserDTO(Long id, String name, String email, Role role) {
        this.id = id; this.name = name; this.email = email; this.role = role;
    }

    public UserDTO(Long id, String name, String email, Role role,
                   String address, Double lat, Double lng, String city, String state, String postalCode, String country) {
        this.id = id; this.name = name; this.email = email; this.role = role;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
    }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

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
