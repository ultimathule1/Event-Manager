package dev.eventmanager.users;

import org.springframework.stereotype.Component;

@Component
public class UserDtoMapping {
    public UserDto toDto(User user) {
        return new UserDto(
                user.id(),
                user.login(),
                user.age(),
                user.role().name()
        );
    }

    public User toDomain(UserDto userDto) {
        return new User(
                userDto.id(),
                userDto.login(),
                userDto.age(),
                UserRole.valueOf(userDto.role())
        );
    }
}
