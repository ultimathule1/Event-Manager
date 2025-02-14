package dev.eventmanager.users;

import dev.eventmanager.RootTest;
import dev.eventmanager.users.api.UserDto;
import dev.eventmanager.users.api.UserRegistration;
import dev.eventmanager.users.db.UserEntity;
import dev.eventmanager.users.db.UserRepository;
import dev.eventmanager.users.domain.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserEventControllerTest extends RootTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void shouldSuccessCreateUser() throws Exception {
        UserRegistration userRegistration = getDummyUserRegistration();

        String createdUserJson = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistration))
                        .header("Authorization", getAuthorizationHeader(UserRole.USER))
                )
                .andExpect(status().is(HttpStatus.CREATED.value()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        var createdDtoUser = objectMapper.readValue(createdUserJson, UserDto.class);

        Assertions.assertNotNull(createdDtoUser.id());
        Assertions.assertEquals(userRegistration.login(), createdDtoUser.login());
        Assertions.assertEquals(userRegistration.age(), createdDtoUser.age());
        Assertions.assertEquals(createdDtoUser.role(), UserRole.USER.name());
    }

    @Test
    public void shouldSuccessGetUserByIdWithAdminAuthority() throws Exception {
        var userEntity = getDummyUserEntity(UserRole.ADMIN);

        var createdUser = userRepository.save(userEntity);

        mockMvc.perform(get("/users/%s".formatted(createdUser.getId()))
                        .header("Authorization",
                                getAuthorizationHeader(UserRole.valueOf(createdUser.getRole())))
                )
                .andExpect(status().is(HttpStatus.OK.value()));
    }

    @Test
    public void shouldFailedGetUserByIdWithUserAuthority() throws Exception {
        var userEntity = getDummyUserEntity(UserRole.USER);

        var createdUser = userRepository.save(userEntity);

        mockMvc.perform(get("/users/%s".formatted(createdUser.getId()))
                        .header("Authorization",
                                getAuthorizationHeader(UserRole.valueOf(createdUser.getRole()))))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }


    private UserRegistration getDummyUserRegistration() {
        return new UserRegistration(
                "vasya-" + getRandomInt(),
                passwordEncoder.encode("vasya"),
                33
        );
    }

    private UserEntity getDummyUserEntity(UserRole userRole) {
        return new UserEntity(
                null,
                "vasya-" + getRandomInt(),
                28,
                passwordEncoder.encode("vasya"),
                userRole.name()
        );
    }

}
