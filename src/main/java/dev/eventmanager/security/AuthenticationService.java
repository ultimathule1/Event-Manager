package dev.eventmanager.security;

import dev.eventmanager.security.jwt.JwtTokenManager;
import dev.eventmanager.users.api.SignInRequest;
import dev.eventmanager.users.domain.UserRole;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenManager jwtTokenManager;

    public AuthenticationService(AuthenticationManager authenticationManager, JwtTokenManager jwtTokenManager) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenManager = jwtTokenManager;
    }

    public String authenticate(SignInRequest signInRequest, UserRole userRole) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signInRequest.login(),
                        signInRequest.password()
                )
        );

        return jwtTokenManager.generateToken(signInRequest.login(), userRole);
    }
}
