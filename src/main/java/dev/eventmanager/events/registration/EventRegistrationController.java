package dev.eventmanager.events.registration;

import dev.eventmanager.users.domain.AuthenticationUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events/registrations")
public class EventRegistrationController {

    private final RegistrationService registrationService;
    private final AuthenticationUserService authenticationUserService;

    public EventRegistrationController(RegistrationService registrationService, AuthenticationUserService authenticationUserService) {
        this.registrationService = registrationService;
        this.authenticationUserService = authenticationUserService;
    }

    @PostMapping("/{eventId}")
    public ResponseEntity<Void> registerUserForEvent(
            @PathVariable("eventId") Long eventId) {
        registrationService
                .registerCurrentUserForEvent(authenticationUserService.getAuthenticatedUser(), eventId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cancel/{eventId}")
    public ResponseEntity<Void> cancelRegistration(@PathVariable("eventId") Long eventId) {
        registrationService
                .cancelUserRegistration(authenticationUserService.getAuthenticatedUser(), eventId);

        return ResponseEntity.noContent().build();
    }

}
