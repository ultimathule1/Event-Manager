package dev.eventmanager.events.api;

import dev.eventmanager.events.domain.RegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events/registrations")
public class EventRegistrationController {

    private final RegistrationService registrationService;

    public EventRegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/{eventId}")
    public ResponseEntity<Void> registerUserForEvent(
            @PathVariable("eventId") Long eventId) {
        registrationService.registerUserForEvent(eventId);

        return ResponseEntity.ok().build();
    }
}
