package dev.eventmanager.users.domain;

import dev.eventmanager.users.api.UserRegistration;
import dev.eventmanager.users.db.UserEntity;
import dev.eventmanager.users.db.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEntityMapper userEntityMapper;
    private static final String USER_NOT_FOUND = "User not found";

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserEntityMapper userEntityMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userEntityMapper = userEntityMapper;
    }

    public User registerUser(UserRegistration user) {
        return createUser(user, getDefaultRole());
    }

    public User registerUser(UserRegistration user, UserRole userRole) {
        return createUser(user, userRole);
    }

    public boolean existsByLogin(String login) {
        return userRepository.existsByLogin(login);
    }

    public User getUserById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        return userEntityMapper.toDomain(user);
    }

    public User getUserByLogin(String login) {
        UserEntity user = userRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        return userEntityMapper.toDomain(user);
    }

    private UserRole getDefaultRole() {
        return UserRole.USER;
    }

    private User createUser(UserRegistration user, UserRole userRole) {
        if (userRepository.existsByLogin(user.login())) {
            throw new IllegalArgumentException("Login already exists");
        }

        var userPassword = passwordEncoder.encode(user.password());
        var savedUserEntity = userRepository.save(new UserEntity(
                null,
                user.login(),
                user.age(),
                userPassword,
                userRole.name()
        ));

        log.info("Created new user: id={}, login={}, age={}, userRole={}",
                savedUserEntity.getId(), savedUserEntity.getLogin(), savedUserEntity.getAge(), userRole.name());

        return userEntityMapper.toDomain(savedUserEntity);
    }
}
