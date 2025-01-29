package dev.eventmanager.config;

import dev.eventmanager.events.Event;
import dev.eventmanager.events.EventDto;
import dev.eventmanager.events.EventEntity;
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
                        ctx.getSource().getOccupiedPlaces(),
                        ctx.getSource().getDate(),
                        ctx.getSource().getDuration(),
                        ctx.getSource().getCost(),
                        ctx.getSource().getOwner().getId(),
                        ctx.getSource().getLocation().getId(),
                        ctx.getSource().getStatus(),
                        ctx.getSource().getMaxPlaces()
                        ));

        mapper.createTypeMap(Event.class, EventDto.class)
                .setConverter(ctx -> new EventDto(
                        ctx.getSource().id(),
                        ctx.getSource().name(),
                        ctx.getSource().maxPlaces(),
                        ctx.getSource().date(),
                        ctx.getSource().cost(),
                        ctx.getSource().occupiedPlaces(),
                        ctx.getSource().duration(),
                        ctx.getSource().locationId(),
                        ctx.getSource().ownerId(),
                        ctx.getSource().status()
                ));

        return mapper;
    }
}
