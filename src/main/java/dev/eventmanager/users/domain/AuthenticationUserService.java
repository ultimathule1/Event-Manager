package dev.eventmanager.users.domain;

import dev.eventmanager.security.jwt.JwtTokenManager;
import dev.eventmanager.users.api.SignInRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationUserService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenManager jwtTokenManager;
    private final UserService userService;

    public AuthenticationUserService(AuthenticationManager authenticationManager, JwtTokenManager jwtTokenManager, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenManager = jwtTokenManager;
        this.userService = userService;
    }

    public String authenticate(SignInRequest signInRequest) {
        var findUser = userService.getUserByLogin(signInRequest.login());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signInRequest.login(),
                        signInRequest.password()
                )
        );

        return jwtTokenManager.generateToken(signInRequest.login(), findUser.role());
    }

//    public boolean isAuthenticated() {
//        var authenticatedUser = SecurityContextHolder.getContext().getAuthentication();
//    }
}
