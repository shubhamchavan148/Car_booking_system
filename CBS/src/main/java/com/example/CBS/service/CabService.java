//This service handles operations related to cabs, primarily managed by drivers.
package com.example.CBS.service;

import com.example.CBS.model.Cab;
import com.example.CBS.model.Driver;
import com.example.CBS.model.Location;
import com.example.CBS.repository.CabRepository;
import com.example.CBS.repository.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CabService {

    private final CabRepository cabRepository;
    private final DriverRepository driverRepository;

    public CabService(CabRepository cabRepository, DriverRepository driverRepository) {
        this.cabRepository = cabRepository;
        this.driverRepository = driverRepository;
    }

    @Transactional
    public Cab registerCab(Long driverId, Cab cabDetails) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found with ID: " + driverId));

        if (cabRepository.findByDriver(driver).isPresent()) {
            throw new IllegalArgumentException("Driver already has a registered cab.");
        }
        if (cabRepository.findByLicensePlate(cabDetails.getLicensePlate()).isPresent()) {
            throw new IllegalArgumentException("Cab with this license plate already exists.");
        }

        cabDetails.setDriver(driver);
        cabDetails.setIsActive(true); // Mark cab as active upon registration
        Cab savedCab = cabRepository.save(cabDetails);
        driver.setCab(savedCab); // Associate cab with driver
        driverRepository.save(driver);
        return savedCab;
    }

    @Transactional(readOnly = true)
    public Optional<Cab> getCabById(Long cabId) {
        return cabRepository.findById(cabId);
    }

    @Transactional(readOnly = true)
    public List<Cab> getAllActiveCabs() {
        return cabRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public Optional<Cab> getCabByDriverId(Long driverId) {
        return driverRepository.findById(driverId)
                .map(cabRepository::findByDriver)
                .orElse(Optional.empty());
    }

    @Transactional
    public Cab updateCabDetails(Long cabId, Cab cabDetails) {
        Cab existingCab = cabRepository.findById(cabId)
                .orElseThrow(() -> new RuntimeException("Cab not found with ID: " + cabId));

        // Update allowed fields
        existingCab.setMake(cabDetails.getMake());
        existingCab.setModel(cabDetails.getModel());
        existingCab.setCabType(cabDetails.getCabType());
        existingCab.setCapacity(cabDetails.getCapacity());
        existingCab.setIsActive(cabDetails.getIsActive()); // Driver can deactivate their cab

        // License plate update might require additional checks if it's unique
        if (!existingCab.getLicensePlate().equals(cabDetails.getLicensePlate())) {
            if (cabRepository.findByLicensePlate(cabDetails.getLicensePlate()).isPresent()) {
                throw new IllegalArgumentException("New license plate is already in use.");
            }
            existingCab.setLicensePlate(cabDetails.getLicensePlate());
        }

        return cabRepository.save(existingCab);
    }

    @Transactional
    public void deactivateCab(Long cabId) {
        Cab cab = cabRepository.findById(cabId)
                .orElseThrow(() -> new RuntimeException("Cab not found with ID: " + cabId));
        cab.setIsActive(false);
        cabRepository.save(cab);
    }

    @Transactional
    public void updateDriverLocation(Long driverId, Location newLocation) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found with ID: " + driverId));
        driver.setCurrentLocation(newLocation);
        driverRepository.save(driver);

        // Update cab location as well if cab is associated with driver
        cabRepository.findByDriver(driver).ifPresent(cab -> {
            cab.setCurrentCabLocation(newLocation);
            cabRepository.save(cab);
        });
    }

    @Transactional
    public void updateDriverAvailability(Long driverId, boolean isAvailable) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found with ID: " + driverId));
        driver.setIsAvailable(isAvailable);
        driverRepository.save(driver);
    }
}
