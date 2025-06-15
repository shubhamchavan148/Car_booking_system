//Details about a cab
package com.example.CBS.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cabs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Cab extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String licensePlate;

    @Column(nullable = false)
    private String make;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING) // Store enum as String in DB
    private CabType cabType;

    private Integer capacity; // Max passengers

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", unique = true, nullable = false) // Foreign key to drivers table
    private Driver driver;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride( name = "latitude", column = @Column(name = "cab_latitude")),
            @AttributeOverride( name = "longitude", column = @Column(name = "cab_longitude")),
            @AttributeOverride( name = "address", column = @Column(name = "cab_address"))
    })
    private Location currentCabLocation;

    @Column(nullable = false)
    private Boolean isActive = false; // Is the cab registered and active in the system?

    public enum CabType {
        SEDAN, SUV, HATCHBACK, LUXURY
    }
}
