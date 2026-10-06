package org.springdemo.gestionstock.dto;


import lombok.Data;

@Data
public class CreateUserRequest {
    private String nom;
    private String prenom;
    private String email;
    @lombok.ToString.Exclude
    private String motDePasse;
    private String role; // "ADMIN" ou "ACHETEUR"
}
