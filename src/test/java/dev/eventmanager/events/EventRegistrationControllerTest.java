package dev.eventmanager.events;

import dev.eventmanager.events.registration.RegistrationService;
import dev.eventmanager.users.domain.AuthenticationUserService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EventRegistrationControllerTest.class)
public class EventRegistrationControllerTest {

    @Mock
    private RegistrationService registrationService;

    @Mock
    private AuthenticationUserService authenticationUserService;

    @InjectMocks
    private EventRegistrationControllerTest eventRegistrationControllerTest;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    void shouldRegisterUserForEvent() throws Exception {
        Long eventId = 1L;

        doNothing().when(registrationService).registerCurrentUserForEvent(any(), eq(eventId));

        mockMvc.perform(post("/events/registrations/{eventId}", eventId))
                .andExpect(status().is(HttpStatus.OK.value()));

        verify(registrationService, times(1)).registerCurrentUserForEvent(any(), eq(eventId));
    }
}
