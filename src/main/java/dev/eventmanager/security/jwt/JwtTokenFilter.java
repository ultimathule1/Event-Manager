package dev.eventmanager.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenFilter.class);

    private final JwtTokenManager jwtTokenManager;

    public JwtTokenFilter(JwtTokenManager jwtTokenManager) {
        this.jwtTokenManager = jwtTokenManager;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String jwtToken = authHeader.replace("Bearer ", "");

        String login = null;
        String role = null;
        try {
            if (!jwtTokenManager.isTokenValid(jwtToken)) {
                filterChain.doFilter(request, response);
                return;
            }

            login = jwtTokenManager.getLoginByToken(jwtToken);
            role = jwtTokenManager.getRoleByToken(jwtToken);
        } catch (Exception e) {
            log.error("Error while reading jwt. Invalid JWT", e);
            filterChain.doFilter(request, response);
        }

        var userForAuthentication = new UsernamePasswordAuthenticationToken(
                login,
                null,
                List.of(new SimpleGrantedAuthority(role))
        );
        SecurityContextHolder
                .getContext()
                .setAuthentication(userForAuthentication);
        filterChain.doFilter(request, response);
    }
}
