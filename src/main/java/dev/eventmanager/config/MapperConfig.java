package dev.eventmanager.config;

import dev.eventmanager.events.domain.Event;
import dev.eventmanager.events.api.EventDto;
import dev.eventmanager.events.db.EventEntity;
import dev.eventmanager.events.registration.RegistrationUserEvent;
import dev.eventmanager.events.registration.RegistrationUserEventEntity;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                                .map(e -> new RegistrationUserEvent(e.getId(),e.getUserId(),e.getEvent().getId()))
                                .toList(),
                        ctx.getSource().getStartDate(),
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
                        ctx.getSource().startDate(),
                        ctx.getSource().cost(),
                        ctx.getSource().registrations().size(),
                        ctx.getSource().duration(),
                        ctx.getSource().locationId(),
                        ctx.getSource().ownerId(),
                        ctx.getSource().status()
                ));

        mapper.createTypeMap(RegistrationUserEventEntity.class, RegistrationUserEvent.class)
                .setConverter(ctx -> new RegistrationUserEvent(
                        ctx.getSource().getId(),
                        ctx.getSource().getUserId(),
                        ctx.getSource().getEvent().getId()
                ));

        return mapper;
    }
}
