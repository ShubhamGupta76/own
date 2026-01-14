package com.connect.Team.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret:DevBlockerSecretKeyForJWTTokenGeneration123456789}")
    private String secret;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> {
            Object userId = claims.get("userId");
            if (userId == null) return null;
            if (userId instanceof Integer) return ((Integer) userId).longValue();
            return (Long) userId;
        });
    }
    
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public String extractRole(String token) {
        return extractClaim(token, claims -> {
            try {
                String role = claims.get("role", String.class);
                if (role != null && !role.isEmpty()) {
                    return role;
                }
            } catch (Exception e) {
            }
            
            Object roleObj = claims.get("role");
            if (roleObj == null) {
                return null;
            }
            return roleObj.toString();
        });
    }
    
    public Long extractOrganizationId(String token) {
        return extractClaim(token, claims -> {
            Object orgId = claims.get("organizationId");
            if (orgId == null) return null;
            if (orgId instanceof Integer) return ((Integer) orgId).longValue();
            return (Long) orgId;
        });
    }
    
    public Boolean validateToken(String token) {
        try {
            return !extractClaim(token, Claims::getExpiration).before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}

