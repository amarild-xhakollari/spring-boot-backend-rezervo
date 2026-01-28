package com.myapp.reservations.security;

import com.myapp.reservations.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;

    public static final String BEARER_ = "Bearer ";
    public static final String USER_ID_ATTRIBUTE = "currentUserId";

    @Autowired
    private CustomUserDetailsService customUserDetailsService;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtil.validateToken(jwt)) {
                final UUID userId = jwtUtil.getUserIdFromToken(jwt);
                final UserDetails userDetails;

                if (userId != null) {
                    userDetails = customUserDetailsService.loadUserById(userId);
                    request.setAttribute(USER_ID_ATTRIBUTE, userId);
                } else {
                    final String username = jwtUtil.getUserFromToken(jwt);
                    userDetails = customUserDetailsService.loadUserByUsername(username);
                }

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource()
                        .buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith(BEARER_)) {
            return headerAuth.substring(BEARER_.length());
        }
        return null;
    }
}
