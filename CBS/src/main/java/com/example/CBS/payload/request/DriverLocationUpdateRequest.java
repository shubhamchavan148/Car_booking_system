// DriverLocationUpdateRequest
package com.example.CBS.payload.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DriverLocationUpdateRequest {
    @NotNull(message = "Current location cannot be null")
    @Valid
    private LocationDTO currentLocation;
}
