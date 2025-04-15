package dev.eventmanager.kafka.service;

import dev.eventmanager.events.api.kafka.event.EventChangerEvent;
import dev.eventmanager.events.api.kafka.event.FieldChange;
import dev.eventmanager.events.domain.Event;
import dev.eventmanager.events.registration.RegistrationRepository;
import dev.eventmanager.users.domain.AuthenticationUserService;
import dev.eventmanager.users.domain.User;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.function.Consumer;

@Component
public class KafkaMessageCreator {
    private final RegistrationRepository registrationRepository;
    private final AuthenticationUserService authenticationUserService;

    public KafkaMessageCreator(RegistrationRepository registrationRepository, AuthenticationUserService authenticationUserService) {
        this.registrationRepository = registrationRepository;
        this.authenticationUserService = authenticationUserService;
    }

    public EventChangerEvent createEventMessageForSystem(Event eventBefore, Event eventAfter) {
        EventChangerEvent eventChanger = createEventMessageForSystem(eventBefore);
        updatesAllFields(eventBefore, eventAfter, eventChanger);
        return eventChanger;
    }

    public EventChangerEvent createEventMessageForSystem(Event event) {
        return createBaseMessage(event, null);
    }

    public EventChangerEvent createEventMessageForUser(Event eventBefore, Event eventAfter) {
        EventChangerEvent eventChanger = createEventMessageForUser(eventBefore);
        updatesAllFields(eventBefore, eventAfter, eventChanger);
        return eventChanger;
    }

    public EventChangerEvent createEventMessageForUser(Event event) {
        return createBaseMessage(event, getAuthenticatedUserId());
    }


    private EventChangerEvent createBaseMessage(Event event, Long changedUserId) {
        EventChangerEvent eventMessage = new EventChangerEvent();
        eventMessage.setEventId(event.id());
        eventMessage.setOwnerEventId(event.ownerId());
        eventMessage.setChangedEventByUserId(changedUserId);
        eventMessage.setEventSubscribers(registrationRepository.getAllSubscribersIdWhereEventIdEquals(event.id()));

        return eventMessage;
    }

    private void updatesAllFields(Event eventBefore, Event eventAfter, EventChangerEvent message) {
        updateField(eventBefore.startDate(), eventAfter.startDate(), message::setFieldEventDate);
        updateField(eventBefore.offsetDate(), eventAfter.offsetDate(), message::setFieldEventDateOffset);
        updateField(eventBefore.duration(), eventAfter.duration(), message::setFieldDuration);
        updateField(eventBefore.locationId(), eventAfter.locationId(), message::setFieldLocationId);
        updateField(eventBefore.name(), eventAfter.name(), message::setFieldEventName);
        updateField(eventBefore.cost(), eventAfter.cost(), message::setFieldEventCost);
        updateField(eventBefore.maxPlaces(), eventAfter.maxPlaces(), message::setFieldMaxPlaces);
        updateField(eventBefore.status(), eventAfter.status(), message::setFieldStatus);
    }

    private <T> void updateField(T before, T after, Consumer<FieldChange<T>> setter) {
        if (!Objects.equals(before, after)) {
            setter.accept(new FieldChange<>(before, after));
        }
    }

    private Long getAuthenticatedUserId() {
        User user = authenticationUserService.getAuthenticatedUser();
        return user == null ? null : user.id();
    }
}
