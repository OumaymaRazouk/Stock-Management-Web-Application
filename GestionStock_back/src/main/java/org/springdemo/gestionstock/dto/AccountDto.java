package org.springdemo.gestionstock.dto;

/** Only non-sensitive fields are returned, including for legacy accounts awaiting role assignment. */
public record AccountDto(Long id, String source, String nom, String prenom, String email, String role) {}
