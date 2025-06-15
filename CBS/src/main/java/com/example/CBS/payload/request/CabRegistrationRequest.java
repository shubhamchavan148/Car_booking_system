// CabRegistrationRequest.java
package com.example.CBS.payload.request;

import com.example.CBS.model.Cab;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CabRegistrationRequest {
    @NotBlank(message = "License plate cannot be empty")
    private String licensePlate;

    @NotBlank(message = "Make cannot be empty")
    private String make;

    @NotBlank(message = "Model cannot be empty")
    private String model;

    @NotNull(message = "Cab type cannot be null")
    private Cab.CabType cabType;

    @NotNull(message = "Capacity cannot be null")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;
}
