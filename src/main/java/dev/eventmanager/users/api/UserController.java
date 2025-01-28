package dev.eventmanager.users.api;

import dev.eventmanager.users.domain.AuthenticationUserService;
import dev.eventmanager.users.domain.User;
import dev.eventmanager.users.domain.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final UserDtoMapper userDtoMapper;
    private final AuthenticationUserService authenticationUserService;

    public UserController(UserService userService, UserDtoMapper userDtoMapper, AuthenticationUserService authenticationUserService) {
        this.userService = userService;
        this.userDtoMapper = userDtoMapper;
        this.authenticationUserService = authenticationUserService;
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(
            @RequestBody @Valid UserRegistration userRegistration
    ) {
        log.info("Received request to create user: userRegistration.login={}", userRegistration.login());
        var createdUser = userService.registerUser(userRegistration);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userDtoMapper.toDto(createdUser));
    }

    @GetMapping
    @RequestMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(
            @PathVariable("id") Long id
    ) {
        log.info("Received request to get user by id: {}", id);
        User foundUser = userService.getUserById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userDtoMapper.toDto(foundUser));
    }

    @GetMapping
    @RequestMapping("/auth")
    public ResponseEntity<JwtTokenResponse> authenticateUser(
            @RequestBody @Valid SignInRequest signInRequest
    ) {
        log.info("Received request to authenticate user: SignInRequest.login={}", signInRequest.login());
        String jwt = authenticationUserService.authenticate(signInRequest);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new JwtTokenResponse(jwt));
    }
}