//Handles cab registration and driver-specific cab/location updates.
package com.example.CBS.controller;

import com.example.CBS.model.Cab;
import com.example.CBS.payload.request.CabRegistrationRequest;
import com.example.CBS.payload.request.DriverLocationUpdateRequest;
import com.example.CBS.security.services.UserDetailsImpl;
import com.example.CBS.service.CabService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers/cabs")
public class CabController {

    private final CabService cabService;

    public CabController(CabService cabService) {
        this.cabService = cabService;
    }

    // Helper to get current authenticated user's ID
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) authentication.getPrincipal()).getId();
        }
        throw new IllegalStateException("User not authenticated.");
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Cab> registerCab(@Valid @RequestBody CabRegistrationRequest request) {
        Long driverId = getCurrentUserId(); // The authenticated user's ID is the driver's ID
        Cab cab = new Cab(
                null,
                request.getLicensePlate(),
                request.getMake(),
                request.getModel(),
                request.getCabType(),
                request.getCapacity(),
                null, // Driver will be set by service
                null, // Location will be set by service
                true
        );
        Cab registeredCab = cabService.registerCab(driverId, cab);
        return new ResponseEntity<>(registeredCab, HttpStatus.CREATED);
    }

    @PutMapping("/location")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<String> updateDriverLocation(@Valid @RequestBody DriverLocationUpdateRequest request) {
        Long driverId = getCurrentUserId();
        cabService.updateDriverLocation(driverId, request.getCurrentLocation());
        return ResponseEntity.ok("Driver location updated successfully.");
    }

    @PutMapping("/availability")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<String> updateDriverAvailability(@RequestParam boolean available) {
        Long driverId = getCurrentUserId();
        cabService.updateDriverAvailability(driverId, available);
        return ResponseEntity.ok("Driver availability updated to " + available + ".");
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Cab> getMyCabDetails() {
        Long driverId = getCurrentUserId();
        return cabService.getCabByDriverId(driverId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Admin or public view for active cabs (might be restricted further)
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('RIDER', 'ADMIN')") // Riders might want to see available cabs
    public ResponseEntity<List<Cab>> getAllActiveCabs() {
        List<Cab> activeCabs = cabService.getAllActiveCabs();
        return ResponseEntity.ok(activeCabs);
    }
}
