package dev.eventmanager.locations;

import dev.eventmanager.StarterTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LocationControllerTest extends StarterTest {

    @Autowired
    LocationRepository locationRepository;
    @Autowired
    LocationService locationService;
    @Autowired
    LocationDtoMapper locationDtoMapper;

    @Test
    void shouldSuccessCreateLocation() throws Exception {
        var locationDto = createDummyLocationDto();

        String createdLocationJson = mockMvc.perform(post("/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locationDto))
                )
                .andExpect(status().is(HttpStatus.CREATED.value()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        LocationDto locationDtoResponse = objectMapper.readValue(createdLocationJson, LocationDto.class);
        Assertions.assertNotNull(locationDtoResponse.id());
        Assertions.assertTrue(locationRepository.existsById(locationDtoResponse.id()));
        Assertions.assertEquals(locationDtoResponse.name(), locationDto.name());
    }

    @Test
    void shouldFailCreateLocation() throws Exception {
        var locationDto = new LocationDto(
                null,
                "ArhiLoft",
                "",
                700,
                "особняк с 11-метровыми потолками"
        );

        mockMvc.perform(post("/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locationDto))
                )
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    void shouldGetLocationById() throws Exception {
        Location locationToSave = new Location(
                null,
                "Ледовый Дворец",
                "г. СПб, Пятилеток 1",
                12300,
                "Спортивно-концертный комплекс в Санкт-Петербурге"
        );

        Location createdLocation = locationService.createLocation(locationToSave);

        String locationDtoResponse =
                mockMvc.perform(get("/locations/%s".formatted(createdLocation.id())))
                        .andExpect(status().is(HttpStatus.OK.value()))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        LocationDto locationDto = objectMapper.readValue(locationDtoResponse, LocationDto.class);
        org.assertj.core.api.Assertions.assertThat(locationDtoMapper.toDto(createdLocation))
                .usingRecursiveComparison()
                .isEqualTo(locationDto);
    }

    @Test
    void shouldFailGetLocationById() throws Exception {
        mockMvc.perform(get("/locations/%s".formatted(999)))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void shouldDeleteLocationById() throws Exception {
        LocationDto locationDtoToSave = createDummyLocationDto();

        Location createdLocation = locationService.createLocation(
                locationDtoMapper.toDomain(locationDtoToSave));

        int sizeBeforeDelete = locationRepository.findAll().size();

        mockMvc.perform(delete("/locations/%s".formatted(createdLocation.id())))
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

        Integer sizeAfterDelete = locationRepository.findAll().size();
        Assertions.assertEquals(sizeBeforeDelete - 1, sizeAfterDelete);
    }

    @Test
    void shouldFailDeleteLocationById() throws Exception {
        mockMvc.perform(delete("/locations/%s".formatted(999)))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void shouldUpdateLocationById() throws Exception {
        LocationDto locationDtoToSave = createDummyLocationDto();

        Location createdLocation = locationService.createLocation(
                locationDtoMapper.toDomain(locationDtoToSave)
        );

        Location locationToUpdate = new Location(
                null,
                "Ледовый Дворец",
                "г. СПб, Пятилеток 1",
                12300,
                "Спортивно-концертный комплекс в Санкт-Петербурге"
        );

        String locationDtoResponse =
                mockMvc.perform(put("/locations?id=%s".formatted(createdLocation.id()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(locationToUpdate))
                        )
                        .andExpect(status().is(HttpStatus.OK.value()))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        LocationDto updatedLocationDto = objectMapper.readValue(locationDtoResponse, LocationDto.class);
        org.assertj.core.api.Assertions.assertThat(updatedLocationDto)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(locationToUpdate);
    }

    @Test
    void shouldFailUpdateLocationById() throws Exception {
        LocationDto locationDtoToSave = createDummyLocationDto();

        Location createdLocation = locationService.createLocation(
                locationDtoMapper.toDomain(locationDtoToSave)
        );

        Location locationToUpdate = new Location(
                null,
                "Ледовый Дворец",
                "г. СПб, Пятилеток 1",
                599,
                "Спортивно-концертный комплекс в Санкт-Петербурге"
        );

        mockMvc.perform(put("/locations?id=%s".formatted(createdLocation.id()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locationToUpdate))
        ).andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    private LocationDto createDummyLocationDto() {
        return new LocationDto(
                null,
                "ArhiLoft",
                "г. Москва, Сколковское шоссе, 31",
                600,
                "особняк с 11-метровыми потолками"
        );
    }
}
