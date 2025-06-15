//This service is central to the cab booking system, managing the ride lifecycle.
//It demonstrates complex transactional logic.
package com.example.CBS.service;

import com.example.CBS.model.*;
import com.example.CBS.model.Booking.BookingStatus;
import com.example.CBS.repository.BookingRepository;
import com.example.CBS.repository.CabRepository;
import com.example.CBS.repository.DriverRepository;
import com.example.CBS.repository.RiderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RiderRepository riderRepository;
    private final DriverRepository driverRepository;
    private final CabRepository cabRepository;
    private final PaymentService paymentService; // Inject PaymentService

    public BookingService(BookingRepository bookingRepository,
                          RiderRepository riderRepository,
                          DriverRepository driverRepository,
                          CabRepository cabRepository,
                          PaymentService paymentService) {
        this.bookingRepository = bookingRepository;
        this.riderRepository = riderRepository;
        this.driverRepository = driverRepository;
        this.cabRepository = cabRepository;
        this.paymentService = paymentService;
    }

    @Transactional
    public Booking requestRide(Long riderId, Location pickup, Location dropoff, Cab.CabType preferredCabType) {
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new RuntimeException("Rider not found with ID: " + riderId));

        // 1. Create a new booking with PENDING status
        Booking booking = new Booking();
        booking.setRider(rider);
        booking.setPickupLocation(pickup);
        booking.setDropoffLocation(dropoff);
        booking.setStatus(BookingStatus.PENDING);
        booking.setEstimatedFare(calculateEstimatedFare(pickup, dropoff, preferredCabType)); // Estimate fare
        booking = bookingRepository.save(booking);

        // 2. Find an available driver (simplified logic: nearest driver in real app)
        // For demo, just pick the first available driver of preferred type
        Optional<Driver> availableDriver = driverRepository.findByIsAvailableTrue().stream()
                .filter(driver -> driver.getCab() != null && driver.getCab().getCabType() == preferredCabType && driver.getCab().getIsActive())
                .findFirst();

        if (availableDriver.isPresent()) {
            Driver driver = availableDriver.get();
            Cab cab = driver.getCab();

            // 3. Assign driver and cab to booking, update status to ACCEPTED
            booking.setDriver(driver);
            booking.setCab(cab);
            booking.setStatus(BookingStatus.ACCEPTED);
            bookingRepository.save(booking);

            // 4. Update driver availability (set to unavailable)
            driver.setIsAvailable(false);
            driverRepository.save(driver);

            System.out.println("Ride requested and assigned to Driver " + driver.getUsername() + " for Rider " + rider.getUsername());
        } else {
            booking.setStatus(BookingStatus.NO_DRIVER_FOUND);
            bookingRepository.save(booking);
            throw new RuntimeException("No driver found for your request at the moment. Please try again later.");
        }

        return booking;
    }

    @Transactional
    public Booking driverAcceptsBooking(Long bookingId, Long driverId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));

        if (!booking.getDriver().getId().equals(driverId)) {
            throw new SecurityException("Driver is not authorized to accept this booking.");
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalArgumentException("Booking is not in PENDING status.");
        }

        booking.setStatus(BookingStatus.ACCEPTED);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking driverArrived(Long bookingId, Long driverId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));
        if (!booking.getDriver().getId().equals(driverId)) {
            throw new SecurityException("Driver is not authorized for this booking.");
        }
        if (booking.getStatus() != BookingStatus.ACCEPTED) {
            throw new IllegalArgumentException("Booking is not in ACCEPTED status.");
        }
        booking.setStatus(BookingStatus.ARRIVED);
        return bookingRepository.save(booking);
    }


    @Transactional
    public Booking startRide(Long bookingId, Long driverId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));

        if (!booking.getDriver().getId().equals(driverId)) {
            throw new SecurityException("Driver is not authorized for this booking.");
        }
        if (booking.getStatus() != BookingStatus.ARRIVED) {
            throw new IllegalArgumentException("Booking must be in ARRIVED status to start.");
        }

        booking.setStatus(BookingStatus.STARTED);
        booking.setStartTime(LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking completeRide(Long bookingId, Long driverId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));

        if (!booking.getDriver().getId().equals(driverId)) {
            throw new SecurityException("Driver is not authorized to complete this booking.");
        }
        if (booking.getStatus() != BookingStatus.STARTED) {
            throw new IllegalArgumentException("Booking is not in STARTED status.");
        }

        booking.setEndTime(LocalDateTime.now());
        booking.setStatus(BookingStatus.COMPLETED);
        booking.setActualFare(calculateActualFare(booking)); // Calculate actual fare
        bookingRepository.save(booking);

        // Initiate payment after ride completion
        paymentService.createPaymentForBooking(booking);

        // Set driver back to available
        Driver driver = booking.getDriver();
        driver.setIsAvailable(true);
        driverRepository.save(driver);

        return booking;
    }

    @Transactional
    public Booking cancelBooking(Long bookingId, Long userId) { // userId can be rider or driver
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));

        // Check if the user is authorized to cancel (rider or assigned driver)
        if (!booking.getRider().getId().equals(userId) && (booking.getDriver() == null || !booking.getDriver().getId().equals(userId))) {
            throw new SecurityException("You are not authorized to cancel this booking.");
        }

        // Only allow cancellation if not already started or completed
        if (booking.getStatus() == BookingStatus.STARTED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot cancel a ride that has already started or completed.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking cancelledBooking = bookingRepository.save(booking);

        // If a driver was assigned, make them available again
        if (booking.getDriver() != null) {
            Driver driver = booking.getDriver();
            driver.setIsAvailable(true);
            driverRepository.save(driver);
        }

        // Handle refund if payment was already initiated (conceptual for now)
        if (cancelledBooking.getPayment() != null && cancelledBooking.getPayment().getStatus() == Payment.PaymentStatus.COMPLETED) {
            // paymentService.initiateRefund(cancelledBooking.getPayment()); // Placeholder for refund logic
            System.out.println("Initiating refund for cancelled booking (conceptual). Booking ID: " + bookingId);
        }

        return cancelledBooking;
    }

    @Transactional(readOnly = true)
    public List<Booking> getRiderBookings(Long riderId) {
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new RuntimeException("Rider not found with ID: " + riderId));
        return bookingRepository.findByRiderOrderByCreatedAtDesc(rider);
    }

    @Transactional(readOnly = true)
    public List<Booking> getDriverBookings(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found with ID: " + driverId));
        return bookingRepository.findByDriverOrderByCreatedAtDesc(driver);
    }

    @Transactional(readOnly = true)
    public Optional<Booking> getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId);
    }

    // --- Private Helper Methods ---

    private BigDecimal calculateEstimatedFare(Location pickup, Location dropoff, Cab.CabType cabType) {
        // Simple fare calculation: distance based (placeholder)
        // In a real app: complex logic with actual distance API, time of day, surge pricing, etc.
        double distance = calculateDistance(pickup, dropoff); // Implement a real distance calculation
        BigDecimal baseFare = BigDecimal.valueOf(10.0);
        BigDecimal ratePerKm = BigDecimal.valueOf(2.0);

        // Adjust rate based on cab type
        switch (cabType) {
            case SEDAN: ratePerKm = BigDecimal.valueOf(2.5); break;
            case SUV: ratePerKm = BigDecimal.valueOf(3.5); break;
            case LUXURY: ratePerKm = BigDecimal.valueOf(5.0); break;
            default: break;
        }

        return baseFare.add(ratePerKm.multiply(BigDecimal.valueOf(distance)));
    }

    private BigDecimal calculateActualFare(Booking booking) {
        // For simplicity, let's assume actual fare is same as estimated for now.
        // In a real app: based on actual route taken, traffic, waiting time, etc.
        return booking.getEstimatedFare();
    }

    private double calculateDistance(Location loc1, Location loc2) {
        // Haversine formula for distance between two lat/lon points (simplified)
        // For a real application, use a dedicated geospatial library or API (e.g., Google Maps Distance Matrix API)
        final int R = 6371; // Radius of Earth in kilometers
        double latDistance = Math.toRadians(loc2.getLatitude() - loc1.getLatitude());
        double lonDistance = Math.toRadians(loc2.getLongitude() - loc1.getLongitude());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(loc1.getLatitude())) * Math.cos(Math.toRadians(loc2.getLatitude()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in kilometers
    }
}
