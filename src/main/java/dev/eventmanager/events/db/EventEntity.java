package dev.eventmanager.events.db;

import dev.eventmanager.events.registration.RegistrationUserEventEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
    @Column(name = "date", nullable = false)
    private OffsetDateTime date;
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

    public EventEntity(String name, Integer maxPlaces, OffsetDateTime date, BigDecimal cost, Integer duration, Long locationId, String status, Long ownerId) {
        this.name = name;
        this.maxPlaces = maxPlaces;
        this.date = date;
        this.cost = cost;
        this.duration = duration;
        this.locationId = locationId;
        this.status = status;
        this.ownerId = ownerId;
        registrations = new ArrayList<>();
    }
}
