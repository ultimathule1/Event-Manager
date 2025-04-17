package dev.eventmanager.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.eventmanager.events.api.dto.EventDto;
import dev.eventmanager.events.api.kafka.event.EventChangerEvent;
import dev.eventmanager.events.db.EventEntity;
import dev.eventmanager.events.domain.Event;
import dev.eventmanager.events.registration.RegistrationUserEvent;
import dev.eventmanager.events.registration.RegistrationUserEventEntity;
import dev.eventmanager.retryable_task.RetryableTaskStatus;
import dev.eventmanager.retryable_task.RetryableTaskType;
import dev.eventmanager.retryable_task.db.entities.RetryableTask;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Configuration
public class MapperConfig {

    @Bean
    public ModelMapper getMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        mapper.createTypeMap(EventEntity.class, Event.class)
                .setConverter(ctx -> new Event(
                        ctx.getSource().getId(),
                        ctx.getSource().getName(),
                        ctx.getSource().getRegistrations()
                                .stream()
                                .map(e -> new RegistrationUserEvent(e.getId(), e.getUserId(), e.getEvent().getId()))
                                .toList(),
                        ctx.getSource().getDate(),
                        ctx.getSource().getOffsetDate(),
                        ctx.getSource().getDuration(),
                        ctx.getSource().getCost(),
                        ctx.getSource().getOwnerId(),
                        ctx.getSource().getLocationId(),
                        ctx.getSource().getStatus(),
                        ctx.getSource().getMaxPlaces()
                ));

        mapper.createTypeMap(Event.class, EventDto.class)
                .setConverter(ctx -> new EventDto(
                        ctx.getSource().id(),
                        ctx.getSource().name(),
                        ctx.getSource().maxPlaces(),
                        convertToDate(ctx.getSource().startDate(), ctx.getSource().offsetDate()),
                        ctx.getSource().cost(),
                        ctx.getSource().registrations().size(),
                        ctx.getSource().duration(),
                        ctx.getSource().locationId(),
                        ctx.getSource().ownerId(),
                        ctx.getSource().status()
                ));

        mapper.createTypeMap(EventEntity.class, EventDto.class)
                .setConverter(ctx -> new EventDto(
                        ctx.getSource().getId(),
                        ctx.getSource().getName(),
                        ctx.getSource().getMaxPlaces(),
                        convertToDate(ctx.getSource().getDate(), ctx.getSource().getOffsetDate()),
                        ctx.getSource().getCost(),
                        ctx.getSource().getRegistrations().size(),
                        ctx.getSource().getDuration(),
                        ctx.getSource().getLocationId(),
                        ctx.getSource().getOwnerId(),
                        ctx.getSource().getStatus()
                ));

        mapper.createTypeMap(RegistrationUserEventEntity.class, RegistrationUserEvent.class)
                .setConverter(ctx -> new RegistrationUserEvent(
                        ctx.getSource().getId(),
                        ctx.getSource().getUserId(),
                        ctx.getSource().getEvent().getId()
                ));

        mapper.createTypeMap(EventChangerEvent.class, RetryableTask.class)
                .setConverter(ctx -> {
                    EventChangerEvent event = ctx.getSource();
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.registerModule(new JavaTimeModule());

                    String payload;
                    try {
                        payload = objectMapper.writeValueAsString(event);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Error converting EventChangerEvent to JSON", e);
                    }

                    RetryableTask task = new RetryableTask();
                    task.setId(UUID.randomUUID());
                    task.setVersion(null);
                    Instant now = Instant.now();
                    task.setCreatedAt(now);
                    task.setUpdatedAt(now);
                    task.setRetryTime(now);
                    task.setPayload(payload);
                    task.setType(RetryableTaskType.SEND_CREATE_NOTIFICATION_REQUEST);
                    task.setStatus(RetryableTaskStatus.IN_PROGRESS);

                    return task;
                });

        mapper.createTypeMap(RetryableTask.class, EventChangerEvent.class)
                .setConverter(ctx -> {
                    RetryableTask task = ctx.getSource();
                    EventChangerEvent event;
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.registerModule(new JavaTimeModule());

                    try {
                        event = objectMapper.readValue(task.getPayload(), EventChangerEvent.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to convert JSON to Order", e);
                    }

                    return event;
                });


        return mapper;
    }

    private OffsetDateTime convertToDate(OffsetDateTime offsetDateTime, ZoneOffset zoneOffset) {
        return OffsetDateTime.of(offsetDateTime.toLocalDateTime(), zoneOffset);
    }
}
