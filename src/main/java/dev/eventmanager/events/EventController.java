package dev.eventmanager.events;

import dev.eventmanager.config.MapperConfig;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @GetMapping("/{id}")
    public ResponseEntity<EventDto> getEventById(
            @PathVariable("id") Long id
    ) {
        log.info("Request to get event by id: id={}", id);
        Event event = eventService.getEventById(id);

        return ResponseEntity
                .ok()
                .body(mapperConfig.getMapper().map(event, EventDto.class));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelEvent(
            @PathVariable("id") Long eventId
    ) {
        log.info("Request to delete event by id: eventId={}", eventId);
        eventService.deleteEvent(eventId);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventDto> updateEvent(
            @PathVariable("id") Long id,
            @RequestBody @Valid EventUpdateRequestDto eventUpdateRequestDto
    ) {
        log.info("Request to update event by id: id={}, eventUpdateRequestDto={}", id, eventUpdateRequestDto);
        EventDto updatedEventDto = mapperConfig.getMapper().map(
                eventService.updateEvent(id, eventUpdateRequestDto),
                EventDto.class
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(updatedEventDto);
    }
}