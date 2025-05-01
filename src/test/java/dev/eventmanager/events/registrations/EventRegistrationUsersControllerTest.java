package dev.eventmanager.events.registrations;

import com.fasterxml.jackson.core.type.TypeReference;
import dev.eventmanager.RootTest;
import dev.eventmanager.events.api.dto.EventCreateRequestDto;
import dev.eventmanager.events.api.dto.EventDto;
import dev.eventmanager.events.db.EventRepository;
import dev.eventmanager.events.registration.RegistrationRepository;
import dev.eventmanager.locations.Location;
import dev.eventmanager.locations.LocationRepository;
import dev.eventmanager.locations.LocationService;
import dev.eventmanager.security.jwt.JwtTokenManager;
import dev.eventmanager.users.api.UserRegistration;
import dev.eventmanager.users.domain.User;
import dev.eventmanager.users.domain.UserRole;
import dev.eventmanager.users.domain.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EventRegistrationUsersControllerTest extends RootTest {

    private static final String DUMMY_USER_LOGIN = "user_" + UUID.randomUUID();
    private static final String DUMMY_USER_PASSWORD = "password_" + UUID.randomUUID();
    @Autowired
    LocationService locationService;
    @Autowired
    JwtTokenManager jwtTokenManager;
    @Autowired
    UserService userService;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    LocationRepository locationRepository;
    @Autowired
    RegistrationRepository registrationRepository;

    @BeforeEach
    void cleanUp() {
        registrationRepository.deleteAll();
        eventRepository.deleteAll();
        locationRepository.deleteAll();
    }

    @Test
    void shouldRegistrationUserForEvent() throws Exception {
        Location savedLocation = locationService.createLocation(createDummyLocation());

        var eventCreateRequestDto = createDummyEventCreateRequestDto(savedLocation.id());
        String eventDtoJsonResp = mockMvc.perform(post("/events")
                        .header("Authorization", authorizationDummyUserWithBearerJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventCreateRequestDto))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        EventDto eventDto = objectMapper.readValue(eventDtoJsonResp, EventDto.class);

        mockMvc.perform(post("/events/registrations/%s".formatted(eventDto.id()))
                        .header("Authorization", getAuthorizationHeader(UserRole.USER)))
                .andExpect(status().is(HttpStatus.OK.value()));
    }

    @Test
    void shouldFailToRegistrationUserForEvent() throws Exception {
        Location savedLocation = locationService.createLocation(createDummyLocation());

        var eventCreateRequestDto = createDummyEventCreateRequestDto(savedLocation.id());
        String eventDtoJsonResp = mockMvc.perform(post("/events")
                        .header("Authorization", getAuthorizationHeader(UserRole.USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventCreateRequestDto))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        EventDto eventDto = objectMapper.readValue(eventDtoJsonResp, EventDto.class);

        mockMvc.perform(post("/events/registrations/%s".formatted(eventDto.id()))
                        .header("Authorization", getAuthorizationHeader(UserRole.USER)))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    void shouldCancelRegistrationCurrentUserForEvent() throws Exception {
        Location savedLocation = locationService.createLocation(createDummyLocation());

        var eventCreateRequestDto = createDummyEventCreateRequestDto(savedLocation.id());
        String eventDtoJsonResp = mockMvc.perform(post("/events")
                        .header("Authorization", authorizationDummyUserWithBearerJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventCreateRequestDto))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        EventDto eventDto = objectMapper.readValue(eventDtoJsonResp, EventDto.class);

        mockMvc.perform(post("/events/registrations/%s".formatted(eventDto.id()))
                        .header("Authorization", getAuthorizationHeader(UserRole.USER)))
                .andExpect(status().is(HttpStatus.OK.value()));

        mockMvc.perform(delete("/events/registrations/cancel/%s".formatted(eventDto.id()))
                        .header("Authorization", getAuthorizationHeader(UserRole.USER)))
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
    }

    @Test
    void shouldGetAllEventsWhereCurrentUserRegistered() throws Exception {
        Location savedLocation = locationService.createLocation(createDummyLocation());

        var eventCreateRequestDto = createDummyEventCreateRequestDto(savedLocation.id());
        String eventDtoJsonResp = mockMvc.perform(post("/events")
                        .header("Authorization", authorizationDummyUserWithBearerJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventCreateRequestDto))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        EventDto eventDto = objectMapper.readValue(eventDtoJsonResp, EventDto.class);

        var eventCreateRequestDto2 = new EventCreateRequestDto(
                "About Birds",
                100,
                OffsetDateTime
                        .now(ZoneOffset.UTC)
                        .plusMonths(6)
                        .minusDays(10)
                        .withNano(0)
                        .toString(),
                BigDecimal.valueOf(1000).setScale(2, RoundingMode.HALF_UP),
                60,
                savedLocation.id()
        );

        String eventDtoJsonResp2 = mockMvc.perform(post("/events")
                        .header("Authorization", authorizationDummyUserWithBearerJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventCreateRequestDto2))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        EventDto eventDto2 = objectMapper.readValue(eventDtoJsonResp2, EventDto.class);

        mockMvc.perform(post("/events/registrations/%s".formatted(eventDto.id()))
                        .header("Authorization", getAuthorizationHeader(UserRole.USER)))
                .andExpect(status().is(HttpStatus.OK.value()));

        mockMvc.perform(post("/events/registrations/%s".formatted(eventDto2.id()))
                        .header("Authorization", getAuthorizationHeader(UserRole.USER)))
                .andExpect(status().is(HttpStatus.OK.value()));

        String eventsDtoJsonResp = mockMvc.perform(get("/events/registrations/my")
                        .header("Authorization", getAuthorizationHeader(UserRole.USER)))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<EventDto> eventsDtoResp = objectMapper.readValue(
                eventsDtoJsonResp,
                new TypeReference<List<EventDto>>() {
                }
        );

        org.assertj.core.api.Assertions.assertThat(eventsDtoResp.size()).isEqualTo(2);
    }

    @Test
    void shouldGetEmptyArrayRegistrationsForUser() throws Exception {
        Location savedLocation = locationService.createLocation(createDummyLocation());

        var eventCreateRequestDto = createDummyEventCreateRequestDto(savedLocation.id());
        mockMvc.perform(post("/events")
                        .header("Authorization", authorizationDummyUserWithBearerJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventCreateRequestDto))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String eventsDtoJsonResp = mockMvc.perform(get("/events/registrations/my")
                        .header("Authorization", getAuthorizationHeader(UserRole.USER)))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<EventDto> eventsDtoResp = objectMapper.readValue(
                eventsDtoJsonResp,
                new TypeReference<List<EventDto>>() {
                }
        );

        org.assertj.core.api.Assertions.assertThat(eventsDtoResp.size()).isEqualTo(0);
    }


    private Location createDummyLocation() {
        return new Location(
                null,
                "ArhiLoft",
                "г. Москва, Сколковское шоссе, 31",
                600,
                "особняк с 11-метровыми потолками"
        );
    }

    private EventCreateRequestDto createDummyEventCreateRequestDto(Long locationId) {
        return new EventCreateRequestDto(
                "Joker <?> Java Problem conference",
                300,
                OffsetDateTime
                        .now(ZoneOffset.UTC)
                        .plusMonths(9)
                        .minusDays(10)
                        .withNano(0)
                        .toString(),
                BigDecimal.valueOf(1000).setScale(2, RoundingMode.HALF_UP),
                60,
                locationId
        );
    }

    private String authorizationDummyUserWithBearerJwt() {
        if (!userService.existsByLogin(DUMMY_USER_LOGIN)) {
            User currentUser = userService.registerUser(new UserRegistration(
                    DUMMY_USER_LOGIN,
                    DUMMY_USER_PASSWORD,
                    26
            ));

            return "Bearer " + jwtTokenManager.generateToken(currentUser.login(), currentUser.role());
        }

        return "Bearer " + jwtTokenManager.generateToken(DUMMY_USER_LOGIN, UserRole.USER);
    }
}
