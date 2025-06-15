//Represents a ride booking.
package com.example.CBS.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Booking extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rider_id", nullable = false)
    private Rider rider;

    @ManyToOne(fetch = FetchType.LAZY) // Can be null initially if driver not assigned
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY) // Can be null if cab not assigned
    @JoinColumn(name = "cab_id")
    private Cab cab;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "pickup_latitude", nullable = false)),
            @AttributeOverride(name = "longitude", column = @Column(name = "pickup_longitude", nullable = false)),
            @AttributeOverride(name = "address", column = @Column(name = "pickup_address", nullable = false))
    })
    private Location pickupLocation;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "dropoff_latitude", nullable = false)),
            @AttributeOverride(name = "longitude", column = @Column(name = "dropoff_longitude", nullable = false)),
            @AttributeOverride(name = "address", column = @Column(name = "dropoff_address", nullable = false))
    })
    private Location dropoffLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status; // PENDING, ACCEPTED, STARTED, COMPLETED, CANCELLED

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Column(precision = 10, scale = 2) // For storing currency values
    private BigDecimal estimatedFare;

    @Column(precision = 10, scale = 2)
    private BigDecimal actualFare;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment; // One-to-one relationship with Payment

    public enum BookingStatus {
        PENDING,       // Rider requested, waiting for driver
        ACCEPTED,      // Driver accepted, on the way to pickup
        ARRIVED,       // Driver arrived at pickup location
        STARTED,       // Ride has started
        COMPLETED,     // Ride finished, payment pending/completed
        CANCELLED,     // Ride cancelled by rider or driver
        NO_DRIVER_FOUND // No driver available for the request
    }
}
