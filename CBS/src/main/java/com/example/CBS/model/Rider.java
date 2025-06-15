//Specific details for a Rider.
package com.example.CBS.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "riders")
@PrimaryKeyJoinColumn(name = "user_id") // Links to the 'users' table via user_id
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Rider extends User {
    // Rider-specific fields can be added here, e.g.,
    // private String favoriteLocation;

    // Constructors needed for easy creation
    public Rider(String username, String password, String email, String firstName, String lastName) {
        super(null, username, password, email, firstName, lastName, null); // Pass null for ID and roles initially
    }
}
