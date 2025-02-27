package dev.eventmanager.users.api;

import dev.eventmanager.users.domain.User;
import dev.eventmanager.users.domain.UserRole;
import org.springframework.stereotype.Component;

@Component
public class UserDtoMapper {
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
