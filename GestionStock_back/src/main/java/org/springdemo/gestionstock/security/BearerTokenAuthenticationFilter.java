package org.springdemo.gestionstock.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {
    private final TokenService tokens;
    private final UserDetailsService users;

    public BearerTokenAuthenticationFilter(TokenService tokens, UserDetailsService users) {
        this.tokens = tokens;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            TokenService.Session session = tokens.resolve(token);
            if (session != null) {
                try {
                    var user = users.loadUserByUsername(session.email());
                    if (user.isEnabled() && user.isAccountNonLocked() && user.isAccountNonExpired()
                            && user.isCredentialsNonExpired() && session.passwordHash().equals(user.getPassword())) {
                        SecurityContextHolder.getContext().setAuthentication(
                                UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities()));
                    } else {
                        tokens.revoke(token);
                    }
                } catch (UsernameNotFoundException e) {
                    tokens.revoke(token);
                }
            }
        }
        chain.doFilter(request, response);
    }
}