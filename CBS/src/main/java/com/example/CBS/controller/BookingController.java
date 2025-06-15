//Manages the lifecycle of ride bookings.
package com.example.CBS.controller;

import com.example.CBS.model.Booking;
import com.example.CBS.model.Rider;
import com.example.CBS.model.Driver;
import com.example.CBS.payload.request.BookingRequest;
import com.example.CBS.payload.request.LocationDTO;
import com.example.CBS.security.services.UserDetailsImpl;
import com.example.CBS.service.BookingService;
import com.example.CBS.repository.RiderRepository; // For rider conversion
import com.example.CBS.repository.DriverRepository; // For driver conversion
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final RiderRepository riderRepository; // To fetch Rider object for service
    private final DriverRepository driverRepository; // To fetch Driver object for service

    public BookingController(BookingService bookingService, RiderRepository riderRepository, DriverRepository driverRepository) {
        this.bookingService = bookingService;
        this.riderRepository = riderRepository;
        this.driverRepository = driverRepository;
    }

    // Helper to get current authenticated user's ID
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) authentication.getPrincipal()).getId();
        }
        throw new IllegalStateException("User not authenticated.");
    }

    @PostMapping("/request-ride")
    @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<Booking> requestRide(@Valid @RequestBody BookingRequest request) {
        Long riderId = getCurrentUserId();
        // Convert LocationDTO to Location entity
        LocationDTO pickupDTO = request.getPickupLocation();
        LocationDTO dropoffDTO = request.getDropoffLocation();

        Booking booking = bookingService.requestRide(
                riderId,
                new com.cabbooking.cabbookingbackend.model.Location(pickupDTO.getLatitude(), pickupDTO.getLongitude(), pickupDTO.getAddress()),
                new com.cabbooking.cabbookingbackend.model.Location(dropoffDTO.getLatitude(), dropoffDTO.getLongitude(), dropoffDTO.getAddress()),
                request.getPreferredCabType()
        );
        return new ResponseEntity<>(booking, HttpStatus.CREATED);
    }

    @PutMapping("/{bookingId}/accept")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Booking> driverAcceptsBooking(@PathVariable Long bookingId) {
        Long driverId = getCurrentUserId();
        Booking updatedBooking = bookingService.driverAcceptsBooking(bookingId, driverId);
        return ResponseEntity.ok(updatedBooking);
    }

    @PutMapping("/{bookingId}/arrived")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Booking> driverArrived(@PathVariable Long bookingId) {
        Long driverId = getCurrentUserId();
        Booking updatedBooking = bookingService.driverArrived(bookingId, driverId);
        return ResponseEntity.ok(updatedBooking);
    }

    @PutMapping("/{bookingId}/start")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Booking> startRide(@PathVariable Long bookingId) {
        Long driverId = getCurrentUserId();
        Booking updatedBooking = bookingService.startRide(bookingId, driverId);
        return ResponseEntity.ok(updatedBooking);
    }

    @PutMapping("/{bookingId}/complete")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Booking> completeRide(@PathVariable Long bookingId) {
        Long driverId = getCurrentUserId();
        Booking updatedBooking = bookingService.completeRide(bookingId, driverId);
        return ResponseEntity.ok(updatedBooking);
    }

    @PutMapping("/{bookingId}/cancel")
    @PreAuthorize("hasAnyRole('RIDER', 'DRIVER', 'ADMIN')")
    public ResponseEntity<Booking> cancelBooking(@PathVariable Long bookingId) {
        Long userId = getCurrentUserId(); // Can be rider or driver
        Booking cancelledBooking = bookingService.cancelBooking(bookingId, userId);
        return ResponseEntity.ok(cancelledBooking);
    }

    @GetMapping("/my-bookings")
    @PreAuthorize("hasAnyRole('RIDER', 'DRIVER')")
    public ResponseEntity<List<Booking>> getMyBookings() {
        Long userId = getCurrentUserId();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isRider = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_RIDER"));

        if (isRider) {
            return ResponseEntity.ok(bookingService.getRiderBookings(userId));
        } else { // Assuming it's a driver
            return ResponseEntity.ok(bookingService.getDriverBookings(userId));
        }
    }

    @GetMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('RIDER', 'DRIVER', 'ADMIN')")
    public ResponseEntity<Booking> getBookingDetails(@PathVariable Long bookingId) {
        // Here you would add logic to ensure the current user is authorized to view this booking
        // e.g., if current user is rider, check if booking.getRider().getId() == currentUserId
        // if current user is driver, check if booking.getDriver().getId() == currentUserId
        // if admin, allow all.
        // For simplicity, we just return the booking if it exists.
        return bookingService.getBookingById(bookingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Admin endpoint
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Booking>> getAllBookings() {
        // Not implemented in BookingService yet, but for admin, you could add
        // List<Booking> findAll(); method to repository and service.
        return ResponseEntity.ok(bookingService.getBookingRepository().findAll());
    }
}
