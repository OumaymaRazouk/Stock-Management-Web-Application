package org.springdemo.gestionstock.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService users, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(users);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, TokenService tokens,
                                                   UserDetailsService users) throws Exception {
        return http
                .cors(withDefaults())
                // Credentials are sent explicitly in Authorization, never via cookies.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/register").denyAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/admin/**", "/api/acheteurs", "/api/acheteurs/**").hasRole("ADMIN")
                        .requestMatchers("/api/commandes", "/api/commandes/**", "/api/besoins/commande/**")
                            .hasAnyRole("ADMIN", "ACHETEUR")
                        .requestMatchers(HttpMethod.GET, "/api/articles", "/api/articles/**", "/api/fournisseurs",
                                "/api/fournisseurs/**", "/api/besoins", "/api/besoins/**")
                            .hasAnyRole("ADMIN", "ACHETEUR", "COMMERCIAL")
                        .requestMatchers(HttpMethod.POST, "/api/besoins").hasAnyRole("ADMIN", "COMMERCIAL")
                        .requestMatchers("/api/articles", "/api/articles/**", "/api/fournisseurs", "/api/fournisseurs/**",
                                "/api/besoins", "/api/besoins/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()
                        .anyRequest().denyAll())
                .addFilterBefore(new BearerTokenAuthenticationFilter(tokens, users),
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"success\":false,\"message\":\"Authentification requise\"}");
                        })
                        .accessDeniedHandler((request, response, exception) -> {
                            response.setStatus(403);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"success\":false,\"message\":\"Acces refuse\"}");
                        }))
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
