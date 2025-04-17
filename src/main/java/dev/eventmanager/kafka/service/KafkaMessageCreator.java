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
        updateAndCheckFields(eventBefore, eventAfter, eventChanger);
        return eventChanger;
    }

    public EventChangerEvent createEventMessageForSystem(Event event) {
        return createBaseMessage(event, null);
    }

    public EventChangerEvent createEventMessageForUser(Event eventBefore, Event eventAfter) {
        EventChangerEvent eventChanger = createEventMessageForUser(eventBefore);
        updateAndCheckFields(eventBefore, eventAfter, eventChanger);
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
        eventMessage.setOffsetDate(event.offsetDate());
        eventMessage.setEventSubscribers(registrationRepository.getAllSubscribersIdWhereEventIdEquals(event.id()));

        return eventMessage;
    }

    private boolean updatesAllFields(Event eventBefore, Event eventAfter, EventChangerEvent message) {
        boolean updated = false;
        updated |= updateField(eventBefore.startDate(), eventAfter.startDate(), message::setFieldEventDate);
        message.setOffsetDate(eventAfter.offsetDate());
        updated |= updateField(eventBefore.duration(), eventAfter.duration(), message::setFieldDuration);
        updated |= updateField(eventBefore.locationId(), eventAfter.locationId(), message::setFieldLocationId);
        updated |= updateField(eventBefore.name(), eventAfter.name(), message::setFieldEventName);
        updated |= updateField(eventBefore.cost(), eventAfter.cost(), message::setFieldEventCost);
        updated |= updateField(eventBefore.maxPlaces(), eventAfter.maxPlaces(), message::setFieldMaxPlaces);
        updated |= updateField(eventBefore.status(), eventAfter.status(), message::setFieldStatus);

        return updated;
    }

    private <T> boolean updateField(T before, T after, Consumer<FieldChange<T>> setter) {
        if (!Objects.equals(before, after)) {
            setter.accept(new FieldChange<>(before, after));
            return true;
        }

        return false;
    }

    private Long getAuthenticatedUserId() {
        User user = authenticationUserService.getAuthenticatedUser();
        return user == null ? null : user.id();
    }

    private void updateAndCheckFields(Event eventBefore, Event eventAfter, EventChangerEvent message) {
        boolean updated = updatesAllFields(eventBefore, eventAfter, message);
        if (!updated) {
            throw new IllegalArgumentException("There aren't any updated fields");
        }
    }
}
