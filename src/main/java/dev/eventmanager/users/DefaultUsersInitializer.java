package dev.eventmanager.users;

import dev.eventmanager.users.api.UserRegistration;
import dev.eventmanager.users.db.UserEntity;
import dev.eventmanager.users.domain.UserRole;
import dev.eventmanager.users.domain.UserService;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DefaultUsersInitializer {

    private static final String DEFAULT_USER_LOGIN = "user";
    private static final String DEFAULT_USER_PASSWORD = "user";
    private static final String DEFAULT_ADMIN_LOGIN = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin";

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public DefaultUsersInitializer(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onContextStartedCreateDefaultUsers(ContextRefreshedEvent event) {
        var userPassword = passwordEncoder.encode(DEFAULT_USER_PASSWORD);
        var adminPassword = passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD);

        createUser(
                DEFAULT_USER_LOGIN,
                userPassword,
                20,
                UserRole.USER
        );

        createUser(
                DEFAULT_ADMIN_LOGIN,
                adminPassword,
                30,
                UserRole.ADMIN
        );
    }

    private void createUser(String login, String password, int age, UserRole role) {
        UserRegistration userRegistration = new UserRegistration(
                login,
                password,
                age
        );

        userService.registerUser(userRegistration, role);
    }
}
