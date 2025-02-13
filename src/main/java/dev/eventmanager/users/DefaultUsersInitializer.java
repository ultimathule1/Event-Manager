package dev.eventmanager.users;

import dev.eventmanager.users.api.UserRegistration;
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

    public DefaultUsersInitializer(UserService userService) {
        this.userService = userService;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onContextStartedCreateDefaultUsers(ContextRefreshedEvent event) {

        if (!userService.existsByLogin(DEFAULT_USER_LOGIN)) {
            createUser(
                    DEFAULT_USER_LOGIN,
                    DEFAULT_USER_PASSWORD,
                    20,
                    UserRole.USER
            );
        }

        if(!userService.existsByLogin(DEFAULT_ADMIN_LOGIN)) {
            createUser(
                    DEFAULT_ADMIN_LOGIN,
                    DEFAULT_ADMIN_PASSWORD,
                    30,
                    UserRole.ADMIN
            );
        }
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
