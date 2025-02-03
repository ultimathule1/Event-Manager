package dev.eventmanager.events.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "max_places")
    private int maxPlaces;
    @Column(name = "date")
    private OffsetDateTime startDate;
    @Column(name = "cost", scale = 2)
    private BigDecimal cost;
    @Column(name = "duration")
    private int duration;
    @Column(name = "location_id")
    private Long locationId;
    @Column(name = "status")
    private String status;
    @Column(name = "owner_id")
    private Long ownerId;
    @OneToMany(mappedBy = "event")
    private List<RegistrationEventUserEntity> registrations;
}
