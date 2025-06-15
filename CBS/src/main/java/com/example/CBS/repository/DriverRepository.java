package com.example.CBS.repository;

import com.example.CBS.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByLicenseNumber(String licenseNumber);
    List<Driver> findByIsAvailableTrue(); // Find available drivers
    List<Driver> findByRatingGreaterThanEqual(Double rating); // Find drivers above a certain rating
}
