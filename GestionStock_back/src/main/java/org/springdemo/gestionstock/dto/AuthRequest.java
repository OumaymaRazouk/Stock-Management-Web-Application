package org.springdemo.gestionstock.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {
    @NotBlank(message = "Email est requis")
    @Email(message = "Email invalide")
    private String email;

    @NotBlank(message = "Mot de passe est requis")
    @lombok.ToString.Exclude
    private String motDePasse;
}