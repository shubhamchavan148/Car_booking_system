package com.example.CBS;

import com.example.CBS.model.Role;

import com.example.CBS.model.User;

import com.example.CBS.model.Rider;

import com.example.CBS.model.Driver;

import com.example.CBS.model.Cab;

import com.example.CBS.model.Location;

import com.example.CBS.repository.CabRepository;

import com.example.CBS.repository.DriverRepository;

import com.example.CBS.repository.RiderRepository;

import com.example.CBS.repository.RoleRepository;

import com.example.CBS.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
@EnableJpaAuditing // Enable JPA Auditing for createdAt and updatedAt fields
public class CbsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CbsApplication.class, args);
	}

	// CommandLineRunner to pre-populate roles, an admin user, and a sample driver/rider/cab on startup
	@Bean
	public CommandLineRunner demoData(
			UserRepository userRepository,
			RoleRepository roleRepository,
			RiderRepository riderRepository,
			DriverRepository driverRepository,
			CabRepository cabRepository,
			PasswordEncoder passwordEncoder) {
		return args -> {
			// Create roles if they don't exist
			Role adminRole = roleRepository.findByName("ROLE_ADMIN")
					.orElseGet(() -> roleRepository.save(new Role(null, "ROLE_ADMIN")));

			Role riderRole = roleRepository.findByName("ROLE_RIDER")
					.orElseGet(() -> roleRepository.save(new Role(null, "ROLE_RIDER")));

			Role driverRole = roleRepository.findByName("ROLE_DRIVER")
					.orElseGet(() -> roleRepository.save(new Role(null, "ROLE_DRIVER")));

			// Create an admin user if not exists
			if (!userRepository.existsByUsername("admin")) {
				User admin = new User("admin", passwordEncoder.encode("adminpass"), "admin@example.com", "Admin", "User");
				Set<Role> adminRoles = new HashSet<>();
				adminRoles.add(adminRole);
				admin.setRoles(adminRoles);
				userRepository.save(admin);
				System.out.println("Created admin user: admin/adminpass");
			}

			// Create a sample Rider if not exists
			if (!userRepository.existsByUsername("rider1")) {
				Rider rider = new Rider("rider1", passwordEncoder.encode("riderpass"), "rider1@example.com", "John", "Doe");
				Set<Role> riderRoles = new HashSet<>();
				riderRoles.add(riderRole);
				rider.setRoles(riderRoles);
				riderRepository.save(rider);
				System.out.println("Created rider user: rider1/riderpass");
			}

			// Create a sample Driver and his Cab if not exists
			if (!userRepository.existsByUsername("driver1")) {
				Driver driver = new Driver("driver1", passwordEncoder.encode("driverpass"), "driver1@example.com", "Jane", "Smith", "DL12345");
				Set<Role> driverRoles = new HashSet<>();
				driverRoles.add(driverRole);
				driver.setRoles(driverRoles);
				driver.setIsAvailable(true); // Set driver as available
				driver.setCurrentLocation(new Location(19.0760, 72.8777, "Mumbai CST")); // Sample Mumbai location

				driver = driverRepository.save(driver); // Save driver first to get ID

				Cab cab = new Cab(null, "MH01AB1234", "Maruti", "Swift", Cab.CabType.SEDAN, 4, driver, new Location(19.0760, 72.8777, "Mumbai CST"), true);
				cabRepository.save(cab);
				driver.setCab(cab); // Set the cab for the driver
				driverRepository.save(driver); // Update driver with cab association

				System.out.println("Created driver user: driver1/driverpass with cab");
			}
		};
	}
}
