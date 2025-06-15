package com.example.CBS.repository;

import com.example.CBS.model.Booking;
import com.example.CBS.model.Booking.BookingStatus;
import com.example.CBS.model.Driver;
import com.example.CBS.model.Rider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByRiderOrderByCreatedAtDesc(Rider rider);
    List<Booking> findByDriverOrderByCreatedAtDesc(Driver driver);
    Optional<Booking> findByIdAndRider(Long id, Rider rider);
    Optional<Booking> findByIdAndDriver(Long id, Driver driver);
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByStatusAndDriverIsNull(BookingStatus status); // For finding pending bookings without a driver
}
