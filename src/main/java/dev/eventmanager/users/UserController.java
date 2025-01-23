package dev.eventmanager.users;

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
    private final UserDtoMapping userDtoMapping;

    public UserController(UserService userService, UserDtoMapping userDtoMapping) {
        this.userService = userService;
        this.userDtoMapping = userDtoMapping;
    }

    @PostMapping
    public ResponseEntity<UserDto> authenticate(
            @RequestBody @Valid UserRegistration userRegistration
    ) {
        log.info("Received request to authenticate user: login={}", userRegistration.login());
        var createdUser = userService.registerUser(userRegistration);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userDtoMapping.toDto(createdUser));
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
                .body(userDtoMapping.toDto(foundUser));
    }
}