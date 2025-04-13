package dev.eventmanager.events.db;

import dev.eventmanager.events.registration.RegistrationUserEventEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@NamedEntityGraph(
        name = "Event.withRegistrations",
        attributeNodes = @NamedAttributeNode("registrations")
)
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "max_places", nullable = false)
    private Integer maxPlaces;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Column(name = "date", nullable = false)
    private OffsetDateTime date;

    @Setter(AccessLevel.NONE)
    @Convert(converter = ZoneOffsetConverter.class)
    @Column(name = "offset_date", nullable = false)
    private ZoneOffset offsetDate;

    @Column(name = "cost", nullable = false, scale = 2)
    private BigDecimal cost;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @OneToMany(mappedBy = "event")
    private List<RegistrationUserEventEntity> registrations;

    public EventEntity(String name, Integer maxPlaces, OffsetDateTime date, ZoneOffset offsetDate, BigDecimal cost, Integer duration, Long locationId, String status, Long ownerId) {
        this.name = name;
        this.maxPlaces = maxPlaces;
        this.date = date;
        this.offsetDate = offsetDate;
        this.cost = cost;
        this.duration = duration;
        this.locationId = locationId;
        this.status = status;
        this.ownerId = ownerId;
        registrations = new ArrayList<>();
    }

    public OffsetDateTime getDate() {
        OffsetDateTime tempDate = OffsetDateTime.of(date.toLocalDateTime(), offsetDate);
        return OffsetDateTime
                .of(tempDate.plusSeconds(offsetDate.getTotalSeconds()).toLocalDateTime(), this.offsetDate);
    }

    public void setDate(OffsetDateTime localDate) {
        this.date = localDate.minusSeconds(localDate.getOffset().getTotalSeconds());
        this.offsetDate = localDate.getOffset();
    }

    @PrePersist
    private void beforePersist() {
        date = date.minusSeconds(offsetDate.getTotalSeconds());
        date = OffsetDateTime.of(date.toLocalDateTime(), ZoneOffset.of("+00:00"));
    }
}
