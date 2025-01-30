package dev.eventmanager.events;

import dev.eventmanager.config.MapperConfig;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
public class EventController {

    private static final Logger log = LoggerFactory.getLogger(EventController.class);
    private final EventService eventService;
    private final MapperConfig mapperConfig;

    public EventController(EventService eventService, MapperConfig mapperConfig) {
            this.eventService = eventService;
            this.mapperConfig = mapperConfig;
        }

        @PostMapping
        public ResponseEntity<EventDto> createEvent(
                @RequestBody @Valid EventCreateRequestDto eventCreateRequestDto
    ) {
            log.info("Request to create event: eventCreateRequestDto={}", eventCreateRequestDto);
            Event event = eventService.createEvent(eventCreateRequestDto);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(mapperConfig.getMapper().map(event, EventDto.class));
        }
    }