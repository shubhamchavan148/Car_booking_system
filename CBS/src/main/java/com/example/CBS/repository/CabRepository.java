package com.example.CBS.repository;

import com.example.CBS.model.Cab;
import com.example.CBS.model.Cab.CabType;
import com.example.CBS.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CabRepository extends JpaRepository<Cab, Long> {
    Optional<Cab> findByLicensePlate(String licensePlate);
    List<Cab> findByIsActiveTrue();
    List<Cab> findByCabTypeAndIsActiveTrue(CabType cabType);
    Optional<Cab> findByDriver(Driver driver);
}
