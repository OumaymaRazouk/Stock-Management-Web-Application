package org.springdemo.gestionstock.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Revocable bearer sessions for one server instance. Restarting invalidates sessions. */
@Service
public class TokenService {
    public record Session(String email, String passwordHash, Instant expiresAt) {
        @Override public String toString() { return "Session[redacted]"; }
    }
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final Duration ttl;
    private final Clock clock;

    @org.springframework.beans.factory.annotation.Autowired
    public TokenService(@Value("${app.auth.token-ttl:PT8H}") Duration ttl) {
        this(ttl, Clock.systemUTC());
    }

    public TokenService(Duration ttl, Clock clock) {
        if (ttl.isZero() || ttl.isNegative() || ttl.compareTo(Duration.ofDays(1)) > 0) {
            throw new IllegalArgumentException("Token TTL must be positive and at most one day");
        }
        this.ttl = ttl;
        this.clock = clock;
    }

    public synchronized String issue(UserDetails user) {
        sessions.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(clock.instant()));
        if (sessions.size() >= 10000) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Session capacity reached");
        }
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessions.put(digest(token), new Session(user.getUsername(), user.getPassword(), clock.instant().plus(ttl)));
        return token;
    }

    public Session resolve(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) return null;
        String key = digest(token);
        Session session = sessions.get(key);
        if (session != null && !session.expiresAt().isAfter(clock.instant())) {
            sessions.remove(key);
            return null;
        }
        return session;
    }

    public void revoke(String token) {
        if (token != null) sessions.remove(digest(token));
    }

    private String digest(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
