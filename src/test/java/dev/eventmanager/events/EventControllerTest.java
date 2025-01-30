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
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EventControllerTest extends RootTest {

    @Autowired
    private LocationService locationService;

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    void shouldSuccessCreateEvent() throws Exception {

        Location location = locationService.createLocation(
                new Location(
                        null,
                        "ArhiLoft",
                        "г. Москва, Сколковское шоссе, 31",
                        600,
                        "особняк с 11-метровыми потолками"
                )
        );

        var eventCreateRequestDto = new EventCreateRequestDto(
                "Joker <?> Java Problem conference",
                500,
                LocalDateTime.now().plusMonths(9).minusDays(10),
                new BigDecimal(String.valueOf(1000)),
                60,
                location.id()
        );

        String createdEventJsonResp = mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventCreateRequestDto))
                )
                .andExpect(status().is(HttpStatus.CREATED.value()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        EventDto eventDto = objectMapper.readValue(createdEventJsonResp, EventDto.class);

        Assertions.assertEquals(eventDto.status(), EventStatus.WAIT_START.name());
        org.assertj.core.api.Assertions.assertThat(eventDto)
                .usingRecursiveComparison()
                .isEqualTo(eventCreateRequestDto);
    }
}
