package alison.customeheadware.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        try {
            Claims claims = jwtUtil.extractAllClaims(jwt);
            
            logger.info("✓ JWT parsed successfully!");
            logger.info("All claims: {}", claims);
            
            // Extract username - try 'sub' first, then 'email'
            String username = claims.getSubject();
            if (username == null || username.isEmpty()) {
                username = claims.get("email", String.class);
            }
            logger.info("Username: {}", username);
            
            // Extract user ID from token
            Long userId = null;
            Object userIdObj = claims.get("userId"); // or "id", "user_id" - match your token
            if (userIdObj != null) {
                if (userIdObj instanceof Number) {
                    userId = ((Number) userIdObj).longValue();
                } else if (userIdObj instanceof String) {
                    userId = Long.parseLong((String) userIdObj);
                }
            }
            logger.info("User ID: {}", userId);
            
            // Extract email
            String email = claims.get("email", String.class);
            logger.info("Email: {}", email);
            
            // Extract role from 'tokenType', 'roles', 'role', or 'authorities'
            Object rolesObj = claims.get("tokenType");  // Your Node.js uses this
            if (rolesObj == null) rolesObj = claims.get("roles");
            if (rolesObj == null) rolesObj = claims.get("role");
            if (rolesObj == null) rolesObj = claims.get("authorities");
            
            logger.info("Roles object: {}", rolesObj);
            
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            String role = null;
            
            if (rolesObj instanceof List) {
                // Handle array of roles
                List<?> rolesList = (List<?>) rolesObj;
                authorities = rolesList.stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r.toString().toUpperCase()))
                    .collect(Collectors.toList());
                if (!rolesList.isEmpty()) {
                    role = rolesList.get(0).toString();
                }
            } else if (rolesObj instanceof String) {
                // Handle single role as string (like tokenType: "ADMIN")
                role = (String) rolesObj;
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
            } else {
                logger.warn("⚠️ No roles found in JWT token!");
            }
            
            logger.info("Extracted authorities: {}", authorities);
            
            // Set authentication if we have username and token is not expired
            if (username != null && !username.isEmpty() && !jwtUtil.isTokenExpired(jwt)) {
                
                // Create custom UserDetails with all extracted data
                JwtUserDetails userDetails = new JwtUserDetails(
                    userId,
                    username,
                    email,
                    role,
                    authorities
                );
                
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                logger.info("✓ Authentication set successfully!");
                logger.info("✓ User ID: {}, Username: {}, Email: {}, Role: {}, Authorities: {}", 
                    userId, username, email, role, authorities);
            } else {
                logger.warn("⚠️ Cannot set authentication - username: {}, expired: {}", 
                    username, jwtUtil.isTokenExpired(jwt));
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            logger.error("❌ JWT token expired: {}", e.getMessage());
        } catch (io.jsonwebtoken.security.SignatureException e) {
            logger.error("❌ JWT signature validation failed: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("❌ JWT validation failed: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }
}
