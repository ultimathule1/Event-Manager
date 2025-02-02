package dev.eventmanager.events;

import com.fasterxml.jackson.core.type.TypeReference;
import dev.eventmanager.RootTest;
import dev.eventmanager.locations.Location;
import dev.eventmanager.locations.LocationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void shouldFailToGetEventById() throws Exception {
        Location savedLocation = locationService.createLocation(createDummyLocation());
        var eventCreateRequestDto = createDummyEventCreateRequestDto(savedLocation.id());
        Event savedEvent = eventService.createEvent(eventCreateRequestDto);

        mockMvc.perform(get("/events/%s".formatted(savedEvent.id() + 1)))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    void shouldSuccessCancelEventById() throws Exception {
        Location savedLocation = locationService.createLocation(createDummyLocation());
        var eventCreateRequestDto = createDummyEventCreateRequestDto(savedLocation.id());
        Event savedEvent = eventService.createEvent(eventCreateRequestDto);

        mockMvc.perform(delete("/events/%s".formatted(savedEvent.id())))
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

        var cancelledEvent = eventService.getEventById(savedEvent.id());

        org.assertj.core.api.Assertions.assertThat(cancelledEvent)
                .usingRecursiveComparison()
                .ignoringFields("status")
                .isEqualTo(savedEvent);
        Assertions.assertNotEquals(savedEvent.status(), cancelledEvent.status());
        Assertions.assertEquals(cancelledEvent.status(), EventStatus.CANCELLED.name());
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void shouldFailToCancelEventById() throws Exception {
        Location savedLocation = locationService.createLocation(createDummyLocation());
        var eventCreateRequestDto = createDummyEventCreateRequestDto(savedLocation.id());
        Event savedEvent = eventService.createEvent(eventCreateRequestDto);

        mockMvc.perform(delete("/events/%s".formatted(savedEvent.id() + 1)))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    void shouldSuccessUpdateEvent() throws Exception {
        Location savedLocation = locationService.createLocation(createDummyLocation());
        var eventCreateRequestDto = createDummyEventCreateRequestDto(savedLocation.id());
        Event savedEvent = eventService.createEvent(eventCreateRequestDto);

        var eventUpdateRequestDto = new EventUpdateRequest(
                "new Event",
                400,
                OffsetDateTime
                        .now(ZoneOffset.UTC)
                        .plusMonths(8)
                        .plusDays(14)
                        .withNano(0),
                BigDecimal.valueOf(1000).setScale(2, RoundingMode.HALF_UP),
                70,
                savedLocation.id()
        );

        String updatedEventJson = mockMvc.perform(put("/events/%s".formatted(savedEvent.id()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventUpdateRequestDto))
                )
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        EventDto updatedEvent = objectMapper.readValue(updatedEventJson, EventDto.class);

        Assertions.assertEquals(savedEvent.status(), updatedEvent.status());
        Assertions.assertEquals(savedEvent.ownerId(), updatedEvent.ownerId());
        Assertions.assertEquals(savedEvent.id(), updatedEvent.id());
        Assertions.assertEquals(savedEvent.occupiedPlaces(), updatedEvent.occupiedPlaces());

        Assertions.assertEquals(eventUpdateRequestDto.eventName(), updatedEvent.name());
        Assertions.assertEquals(eventUpdateRequestDto.maxPlaces(), updatedEvent.maxPlaces());
        Assertions.assertEquals(eventUpdateRequestDto.cost(), updatedEvent.cost());
        Assertions.assertEquals(eventUpdateRequestDto.locationId(), updatedEvent.locationId());
        Assertions.assertEquals(eventUpdateRequestDto.duration(), updatedEvent.duration());
        Assertions.assertEquals(eventUpdateRequestDto.startDate(), updatedEvent.date());
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    void shouldSuccessUpdateEventOnlyName() throws Exception {
        Location savedLocation = locationService.createLocation(createDummyLocation());
        var eventCreateRequestDto = createDummyEventCreateRequestDto(savedLocation.id());
        Event savedEvent = eventService.createEvent(eventCreateRequestDto);

        var eventUpdateRequestDto = new EventUpdateRequest(
                "About New Something",
                null,
                null,
                null,
                null,
                null
        );

        String updatedEventDtoRespJson =
                mockMvc.perform(put("/events/%s".formatted(savedEvent.id()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(eventUpdateRequestDto)))
                        .andExpect(status().is(HttpStatus.OK.value()))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        EventDto updatedEventDtoResp = objectMapper.readValue(updatedEventDtoRespJson, EventDto.class);

        Assertions.assertEquals(updatedEventDtoResp.name(), eventUpdateRequestDto.eventName());
        org.assertj.core.api.Assertions.assertThat(eventService.getEventById(savedEvent.id()))
                .usingRecursiveComparison()
                .ignoringFields("name")
                .isEqualTo(eventService.getEventById(updatedEventDtoResp.id()));
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    void shouldFailUpdateEventBecauseInvalidMaxPlacesInRequest() throws Exception {
        Location savedLocation = locationService.createLocation(createDummyLocation());
        var eventCreateRequestDto = createDummyEventCreateRequestDto(savedLocation.id());
        Event savedEvent = eventService.createEvent(eventCreateRequestDto);

        var eventUpdateRequestDto = new EventUpdateRequest(
                "new Event",
                299,
                OffsetDateTime
                        .now(ZoneOffset.UTC)
                        .plusMonths(8)
                        .plusDays(14)
                        .withNano(0),
                BigDecimal.valueOf(1000).setScale(2, RoundingMode.HALF_UP),
                70,
                savedLocation.id()
        );

        mockMvc.perform(put("/events/%s".formatted(savedEvent.id()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventUpdateRequestDto)))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    void shouldFailUpdateEventBecauseInvalidLocationIdInRequest() throws Exception {
        Location savedLocation = locationService.createLocation(createDummyLocation());
        var eventCreateRequestDto = createDummyEventCreateRequestDto(savedLocation.id());
        Event savedEvent = eventService.createEvent(eventCreateRequestDto);

        var eventUpdateRequestDto = new EventUpdateRequest(
                "new Event",
                299,
                OffsetDateTime
                        .now(ZoneOffset.UTC)
                        .plusMonths(8)
                        .plusDays(14)
                        .withNano(0),
                BigDecimal.valueOf(1000).setScale(2, RoundingMode.HALF_UP),
                70,
                savedLocation.id() + 1
        );

        mockMvc.perform(put("/events/%s".formatted(savedEvent.id()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventUpdateRequestDto)))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    void shouldGetCreatedEventsByCurrentUser() throws Exception {
        Location savedLocation = locationService.createLocation(createDummyLocation());
        var eventCreateRequestDto = createDummyEventCreateRequestDto(savedLocation.id());
        Event savedEvent = eventService.createEvent(eventCreateRequestDto);
        Event savedEvent2 = eventService.createEvent(eventCreateRequestDto);

        String eventsDtoRespJson = mockMvc.perform(get("/events/my"))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<EventDto> eventDtos = objectMapper.readValue(
                eventsDtoRespJson,
                new TypeReference<List<EventDto>>() {
                }
        );

        org.assertj.core.api.Assertions.assertThat(eventDtos).hasSize(2);
        org.assertj.core.api.Assertions.assertThat(mapperConfig.getMapper().map(savedEvent, EventDto.class))
                .usingRecursiveComparison()
                .isEqualTo(eventDtos.getFirst());
        org.assertj.core.api.Assertions.assertThat(mapperConfig.getMapper().map(savedEvent2, EventDto.class))
                .usingRecursiveComparison()
                .isEqualTo(eventDtos.getLast());
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    void shouldGetEmptyArrayForCurrentUser() throws Exception {
        String eventsDtoRespJson = mockMvc.perform(get("/events/my"))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<EventDto> eventDtos = objectMapper.readValue(
                eventsDtoRespJson,
                new TypeReference<List<EventDto>>() {
                }
        );

        org.assertj.core.api.Assertions.assertThat(eventDtos).isEmpty();
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
                        .withNano(0),
                BigDecimal.valueOf(1000).setScale(2, RoundingMode.HALF_UP),
                60,
                locationId
        );
    }
}
