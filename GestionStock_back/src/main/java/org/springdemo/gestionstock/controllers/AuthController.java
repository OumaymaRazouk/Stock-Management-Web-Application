package org.springdemo.gestionstock.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springdemo.gestionstock.dto.AuthRequest;
import org.springdemo.gestionstock.dto.AuthResponse;
import org.springdemo.gestionstock.security.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserDetailsService details;
    private final AuthenticationManager authentication;
    private final TokenService tokens;

    public AuthController(UserDetailsService details, AuthenticationManager authentication, TokenService tokens) {
        this.details = details;
        this.authentication = authentication;
        this.tokens = tokens;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        try {
            var authenticated = authentication.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse()));
            var principal = details.loadUserByUsername(authenticated.getName());
            String role = principal.getAuthorities().iterator().next().getAuthority().substring(5);
            return ResponseEntity.ok(new AuthResponse(true, "Connexion réussie", tokens.issue(principal), role));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(new AuthResponse(false, "Identifiants incorrects ou rôle à faire attribuer par un administrateur"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) tokens.revoke(header.substring(7));
        return ResponseEntity.noContent().build();
    }
}
