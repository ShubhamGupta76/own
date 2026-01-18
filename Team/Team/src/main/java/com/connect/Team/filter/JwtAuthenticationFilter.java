package com.connect.Team.filter;

import com.connect.Team.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 */
@Component
@RequiredArgsConstructor
@Slf4j
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
                Long organizationId = jwtUtil.extractOrganizationId(token);
                
                log.info("JWT token processed - UserId: {}, Email: {}, Role: {}, OrganizationId: {}", 
                        userId, email, role, organizationId);
                
                if (role == null || role.isEmpty()) {
                    log.warn("JWT token missing role claim for email: {}, userId: {}", email, userId);
                    filterChain.doFilter(request, response);
                    return;
                }
                
                if (organizationId == null || organizationId == 0) {
                    log.warn("JWT token missing organizationId for email: {}, userId: {}, role: {}. User may need to log out and log back in.", 
                            email, userId, role);
                }
                
                String normalizedRole = role.trim().toUpperCase();
                log.debug("Setting authentication for user: {} with role: {} (normalized: {})", email, role, normalizedRole);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + normalizedRole))
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authentication set successfully for user: {} with authority: ROLE_{}", email, normalizedRole);
            } else {
                log.warn("JWT token validation failed for request: {}", request.getRequestURI());
            }
        } catch (Exception e) {
            log.error("Error processing JWT token for request: {}. Error: {}", request.getRequestURI(), e.getMessage(), e);
        }
        
        filterChain.doFilter(request, response);
    }
}

