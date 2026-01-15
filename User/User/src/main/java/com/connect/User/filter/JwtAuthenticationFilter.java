package com.connect.User.filter;

import com.connect.User.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT Authentication Filter
 * Validates JWT token and sets authentication context
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            final String token = authHeader.substring(7);
            
            if (jwtUtil.validateToken(token)) {
                String role = jwtUtil.extractRole(token);
                String email = jwtUtil.extractEmail(token);
                Long userId = jwtUtil.extractUserId(token);
                
                // Set authentication context
                String authority = "ROLE_" + role;
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(authority))
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // Debug logging
                System.out.println("JWT Filter: Set authentication for user: " + email + ", role: " + role + ", authority: " + authority);
                System.out.println("JWT Filter: SecurityContext authentication: " + SecurityContextHolder.getContext().getAuthentication());
            } else {
                System.out.println("JWT Filter: Token validation failed");
            }
        } catch (Exception e) {
            // Token validation failed, continue without authentication
            System.out.println("JWT Filter: Exception during token validation: " + e.getMessage());
            System.out.println("JWT Filter: Exception type: " + e.getClass().getName());
            e.printStackTrace();
        }
        
        filterChain.doFilter(request, response);
    }
}

