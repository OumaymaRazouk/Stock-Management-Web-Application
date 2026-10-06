package org.springdemo.gestionstock.services;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Set;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserService users;
    private final AcheteurService acheteurs;

    public UserDetailsServiceImpl(UserService users, AcheteurService acheteurs) {
        this.users = users;
        this.acheteurs = acheteurs;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        var user = users.findByEmail(email);
        if (user != null) return principal(user.getEmail(), user.getMotDePasse(), user.getRole());
        var acheteur = acheteurs.findByEmail(email);
        if (acheteur != null) return principal(acheteur.getEmail(), acheteur.getMotDePasse(), acheteur.getRole());
        throw new UsernameNotFoundException("Invalid credentials");
    }

    private UserDetails principal(String email, String hash, String role) {
        if (role == null || !Set.of("ADMIN", "ACHETEUR", "COMMERCIAL").contains(role)) {
            throw new UsernameNotFoundException("Unsupported role");
        }
        return User.withUsername(email).password(hash).roles(role).build();
    }
}