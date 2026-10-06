package org.springdemo.gestionstock.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private boolean success;
    private String message;
    @lombok.ToString.Exclude
    private String token;
    private String role;

    public AuthResponse(boolean success, String message, String role) {
        this.success = success;
        this.message = message;
        this.token = "";
        this.role = role;
    }
    public AuthResponse(boolean success, String message) {
        this(success, message, "", "");
    }

}
