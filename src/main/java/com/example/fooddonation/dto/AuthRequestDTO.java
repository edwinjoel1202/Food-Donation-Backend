// filename: src/main/java/com/example/fooddonation/dto/AuthRequestDTO.java
package com.example.fooddonation.dto;

public class AuthRequestDTO {
    private String email;
    private String password;
    private String name;
    private String role; // "USER", "VOLUNTEER", "ADMIN" (optional on register)

    // getters & setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}