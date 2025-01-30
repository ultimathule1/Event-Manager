package dev.eventmanager.events;

import dev.eventmanager.RootTest;
import dev.eventmanager.locations.Location;
import dev.eventmanager.locations.LocationEntity;
import dev.eventmanager.locations.LocationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EventControllerTest extends RootTest {

    @Autowired
    private LocationService locationService;

    @Autowired
    private EventService eventService;

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    void shouldSuccessCreateEvent() throws Exception {

        Location location = locationService.createLocation(createDummyLocation());

        var eventCreateRequestDto = createDummyEventCreateRequestDto(location.id());

        String createdEventJsonResp = mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventCreateRequestDto))
                )
                .andExpect(status().is(HttpStatus.CREATED.value()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        EventDto eventDtoResp = objectMapper.readValue(createdEventJsonResp, EventDto.class);

        Assertions.assertEquals(eventDtoResp.status(), EventStatus.WAIT_START.name());
        Assertions.assertEquals(location.id(), eventDtoResp.locationId());
        Assertions.assertNotNull(eventDtoResp.id());
        org.assertj.core.api.Assertions.assertThat(eventDtoResp)
                .usingRecursiveComparison()
                .ignoringFields("id", "occupiedPlaces", "ownerId", "status")
                .isEqualTo(eventCreateRequestDto);
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void shouldFailCreateEventBecauseForbidden() throws Exception {
        Location location = createDummyLocation();
        locationService.createLocation(location);
        var eventCreateRequestDto = createDummyEventCreateRequestDto(location.id());

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventCreateRequestDto))
                )
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()))
        ;
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    void shouldFailCreateEventBecauseInvalidEventValues() throws Exception {
        Location savedLocation = locationService.createLocation(createDummyLocation());
        var eventCreateRequestDto = new EventCreateRequestDto(
                "Joker <?> Java Problem conference",
                500,
                OffsetDateTime
                        .now(ZoneOffset.UTC)
                        .plusMonths(9)
                        .minusDays(10)
                        .withNano(0),
                new BigDecimal(String.valueOf(1000)),
                20,
                savedLocation.id()
        );

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventCreateRequestDto))
                )
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
        ;
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    void shouldFailCreateEventBecauseNotFoundLocation() throws Exception {
        Location savedLocation = locationService.createLocation(createDummyLocation());

        var eventCreateRequestDto = new EventCreateRequestDto(
                "Joker <?> Java Problem conference",
                500,
                OffsetDateTime
                        .now(ZoneOffset.UTC)
                        .plusMonths(9)
                        .minusDays(10)
                        .withNano(0),
                new BigDecimal(String.valueOf(1000)),
                100,
                savedLocation.id() + 1L
        );

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventCreateRequestDto))
                )
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
        ;
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    void shouldGetEventById() throws Exception {
        Location savedLocation = locationService.createLocation(createDummyLocation());
        var eventCreateRequestDto = createDummyEventCreateRequestDto(savedLocation.id());
        Event savedEvent = eventService.createEvent(eventCreateRequestDto);

        String eventDtoJsonResp = mockMvc.perform(get("/events/%s".formatted(savedEvent.id())))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        EventDto eventDtoResp = objectMapper.readValue(eventDtoJsonResp, EventDto.class);

        org.assertj.core.api.Assertions.assertThat(mapperConfig.getMapper().map(eventDtoResp, Event.class))
                .usingRecursiveComparison()
                .isEqualTo(savedEvent);
    }

    @Test
    @WithMockUser(username = "user", authorities = "ADMIN")
    void shouldFailToGetEventById() throws Exception {
        Location savedLocation = locationService.createLocation(createDummyLocation());
        var eventCreateRequestDto = createDummyEventCreateRequestDto(savedLocation.id());
        Event savedEvent = eventService.createEvent(eventCreateRequestDto);

        mockMvc.perform(get("/events/%s".formatted(savedEvent.id() + 1)))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
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
                500,
                OffsetDateTime
                        .now(ZoneOffset.UTC)
                        .plusMonths(9)
                        .minusDays(10)
                        .withNano(0),
                BigDecimal.valueOf(1000).setScale(2, RoundingMode.HALF_UP),
                60,
                locationId
        );
    }
}
