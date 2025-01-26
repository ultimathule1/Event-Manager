package dev.eventmanager.users;

import dev.eventmanager.security.jwt.JwtTokenManager;
import dev.eventmanager.users.db.UserEntity;
import dev.eventmanager.users.db.UserRepository;
import dev.eventmanager.users.domain.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserTestUtils {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenManager jwtTokenManager;

    private static final String DEFAULT_ADMIN_LOGIN = "admin2";
    private static final String DEFAULT_USER_LOGIN = "user2";
    private static volatile boolean isUsersInitialized = false;

    public UserTestUtils(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenManager jwtTokenManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenManager = jwtTokenManager;
    }

    public String getJwtToken(UserRole userRole) {
        if (!isUsersInitialized) {
            initializeTestUsers();
            isUsersInitialized = true;
        }

        return switch (userRole) {
            case ADMIN -> jwtTokenManager.generateToken(DEFAULT_ADMIN_LOGIN, UserRole.ADMIN);
            case USER -> jwtTokenManager.generateToken(DEFAULT_USER_LOGIN, UserRole.USER);
        };
    }

    private void initializeTestUsers() {
        createUser(DEFAULT_ADMIN_LOGIN, 20, "admin_admin", UserRole.ADMIN);
        createUser(DEFAULT_USER_LOGIN, 20, "user_user", UserRole.USER);
    }

    private void createUser(
            String login,
            int age,
            String password,
            UserRole role
    ) {
        String passwordHash = passwordEncoder.encode(password);

        userRepository.save(
                new UserEntity(
                        null,
                        login,
                        age,
                        passwordHash,
                        role.name()
                )
        );
    }
}
