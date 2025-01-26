package dev.eventmanager.users.domain;

import dev.eventmanager.users.api.UserRegistration;
import dev.eventmanager.users.db.UserEntity;
import dev.eventmanager.users.db.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final static String USER_NOT_FOUND = "User not found";

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(UserRegistration user) {
        if (userRepository.existsByLogin(user.login())) {
            throw new IllegalArgumentException("Login already exists");
        }

        var userPassword = passwordEncoder.encode(user.password());
        var savedUserEntity = userRepository.save(new UserEntity(
                null,
                user.login(),
                user.age(),
                userPassword,
                getDefaultRole().name()
        ));

        return toDomain(savedUserEntity);
    }

    public User getUserById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        return toDomain(user);
    }

    public User getUserByLogin(String login) {
        UserEntity user = userRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        return toDomain(user);
    }

    private UserRole getDefaultRole() {
        return UserRole.USER;
    }

    private User toDomain(UserEntity userEntity) {
        return new User(
                userEntity.getId(),
                userEntity.getLogin(),
                userEntity.getAge(),
                UserRole.valueOf(userEntity.getRole())
        );
    }
}
