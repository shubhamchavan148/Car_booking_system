//Specific details for a Driver.
package com.example.CBS.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "drivers")
@PrimaryKeyJoinColumn(name = "user_id") // Links to the 'users' table via user_id
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Driver extends User {
    private String licenseNumber;
    private Double rating = 0.0; // Default rating
    private Integer numberOfRatings = 0; // Number of ratings received

    @OneToOne(mappedBy = "driver", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Cab cab; // One-to-one relationship with Cab

    @Embedded
    @AttributeOverrides({
            @AttributeOverride( name = "latitude", column = @Column(name = "current_latitude")),
            @AttributeOverride( name = "longitude", column = @Column(name = "current_longitude")),
            @AttributeOverride( name = "address", column = @Column(name = "current_address"))
    })
    private Location currentLocation; // Driver's last known location

    @Column(nullable = false)
    private Boolean isAvailable = false; // Driver's availability status

    // Constructor for easy creation
    public Driver(String username, String password, String email, String firstName, String lastName, String licenseNumber) {
        super(null, username, password, email, firstName, lastName, null);
        this.licenseNumber = licenseNumber;
    }

    public void updateRating(double newRating) {
        this.rating = (this.rating * this.numberOfRatings + newRating) / (this.numberOfRatings + 1);
        this.numberOfRatings++;
    }
}
