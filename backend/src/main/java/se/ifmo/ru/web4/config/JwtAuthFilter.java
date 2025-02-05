package se.ifmo.ru.web4.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import se.ifmo.ru.web4.service.JwtService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // убираем "Bearer "

            try {
                Claims claims = jwtService.parseToken(token);
                String username = claims.getSubject();  // username
                String role = (String) claims.get("role");

                // Можно загрузить из базы userDetails, если нужно:
                // UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Аутентификация
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                username, // principal (можно userDetails)
                                null,     // credentials
                                Collections.emptyList() // или new SimpleGrantedAuthority(role)
                        );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception e) {
                // Токен невалиден / просрочен
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}