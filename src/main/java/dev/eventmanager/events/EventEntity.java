package dev.eventmanager.events;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    @Column(name = "maxPlaces")
    private int maxPlaces;
    @Column(name = "occupiedPlaces")
    private int occupiedPlaces;
    @Column(name = "date")
    private LocalDateTime startDate;
    @Column(name = "cost")
    private BigDecimal cost;
    @Column(name = "duration")
    private int duration;
    @Column(name = "locationId")
    private Long locationId;
    @Column(name = "status")
    private String status;
}
