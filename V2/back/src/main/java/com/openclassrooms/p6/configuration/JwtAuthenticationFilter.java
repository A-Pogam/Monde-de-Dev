package com.openclassrooms.p6.configuration;

import com.openclassrooms.p6.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        System.out.println("[JWT FILTER] Interception de la requête : " + request.getRequestURI());


        if (header != null && header.startsWith("Bearer ")) {
            String token = JwtUtil.extractJwtFromHeader(header);
            System.out.println("[JWT FILTER] Token extrait : " + token);


            if (jwtUtil.isTokenValid(token)) {
                System.out.println("[JWT FILTER] Token valide");

                Optional<Long> userIdOpt = jwtUtil.extractUserId(token);
                System.out.println("[JWT FILTER] User ID extrait du token : " + userIdOpt);


                if (userIdOpt.isPresent()) {
                    Long userId = userIdOpt.get();

                    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId.toString(), null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("[JWT FILTER] Authentication injectée dans le contexte");
                } else {
                    System.out.println("[JWT FILTER] Aucun userId trouvé dans le token");
                }
            } else {
                System.out.println("[JWT FILTER] Token invalide");
            }
        } else {
            System.out.println("[JWT FILTER] Aucun header Authorization valide trouvé");
        }

        filterChain.doFilter(request, response);
    }
}
