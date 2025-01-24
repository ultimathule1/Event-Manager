package dev.eventmanager.users;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DefaultAddUsersStarter {

    private static final String DEFAULT_USER_LOGIN = "user";
    private static final String DEFAULT_USER_PASSWORD = "user";
    private static final String DEFAULT_ADMIN_LOGIN = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultAddUsersStarter(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onContextStarted(ContextRefreshedEvent event) {
        var userPassword = passwordEncoder.encode(DEFAULT_USER_PASSWORD);
        var adminPassword = passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD);

        if (!userRepository.existsByLogin(DEFAULT_USER_LOGIN)) {
            userRepository.save(new UserEntity(
                    null,
                    DEFAULT_USER_LOGIN,
                    20,
                    userPassword,
                    UserRole.USER.name()
            ));
        }

        if (!userRepository.existsByLogin(DEFAULT_ADMIN_LOGIN)) {
            userRepository.save(new UserEntity(
                    null,
                    DEFAULT_ADMIN_LOGIN,
                    20,
                    adminPassword,
                    UserRole.ADMIN.name()
            ));
        }
    }
}
