package com.example.CBS.payload.request;

import com.example.CBS.model.Cab;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequest {
    @NotNull(message = "Pickup location cannot be null")
    @Valid // Nested validation for LocationDTO
    private LocationDTO pickupLocation;

    @NotNull(message = "Dropoff location cannot be null")
    @Valid // Nested validation for LocationDTO
    private LocationDTO dropoffLocation;

    @NotNull(message = "Preferred cab type cannot be null")
    private Cab.CabType preferredCabType;
}
