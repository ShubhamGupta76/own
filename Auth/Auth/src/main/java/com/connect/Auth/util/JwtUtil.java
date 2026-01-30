package com.connect.Auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Component
public class JwtUtil {
    
    @Value("${jwt.secret:DevBlockerSecretKeyForJWTTokenGeneration123456789012345678901234567890}")
    private String secret;
    
    @Value("${jwt.expiration:86400000}") 
    private Long expiration;
    
   
    public String generateToken(String userId, String email, String role, String organizationId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("role", role);
        
        if (organizationId != null && !organizationId.isEmpty()) {
            claims.put("organizationId", organizationId);
        }
        
        return createToken(claims, email);
    }
    
    
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }
    
    
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
    
    
    public String extractUserId(String token) {
        return extractClaim(token, claims -> {
            Object userId = claims.get("userId");
            if (userId == null) return null;
            return userId.toString();
        });
    }
    
   
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }
    
    
    public String extractOrganizationId(String token) {
        return extractClaim(token, claims -> {
            Object orgId = claims.get("organizationId");
            if (orgId == null) return null;
            return orgId.toString();
        });
    }
    
    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
   
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
   
    public Boolean validateToken(String token, String email) {
        final String tokenEmail = extractEmail(token);
        return (tokenEmail.equals(email) && !isTokenExpired(token));
    }
}

