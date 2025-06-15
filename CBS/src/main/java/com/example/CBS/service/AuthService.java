//This service handles user registration and authentication logic, including password encoding and assigning default roles.
package com.example.CBS.service;

import com.example.CBS.model.User;
import com.example.CBS.model.Rider;
import com.example.CBS.model.Driver;
import com.example.CBS.model.Role;
import com.example.CBS.repository.UserRepository;
import com.example.CBS.repository.RiderRepository;
import com.example.CBS.repository.DriverRepository;
import com.example.CBS.repository.RoleRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RiderRepository riderRepository;
    private final DriverRepository driverRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager; // For login

    public AuthService(UserRepository userRepository,
                       RiderRepository riderRepository,
                       DriverRepository driverRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.riderRepository = riderRepository;
        this.driverRepository = driverRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public Rider registerRider(Rider rider) {
        if (userRepository.existsByUsername(rider.getUsername())) {
            throw new IllegalArgumentException("Username is already taken!");
        }
        if (userRepository.existsByEmail(rider.getEmail())) {
            throw new IllegalArgumentException("Email is already in use!");
        }

        rider.setPassword(passwordEncoder.encode(rider.getPassword()));
        Role riderRole = roleRepository.findByName("ROLE_RIDER")
                .orElseThrow(() -> new RuntimeException("Error: ROLE_RIDER not found. Please pre-populate roles."));
        Set<Role> roles = new HashSet<>();
        roles.add(riderRole);
        rider.setRoles(roles);

        return riderRepository.save(rider);
    }

    @Transactional
    public Driver registerDriver(Driver driver) {
        if (userRepository.existsByUsername(driver.getUsername())) {
            throw new IllegalArgumentException("Username is already taken!");
        }
        if (userRepository.existsByEmail(driver.getEmail())) {
            throw new IllegalArgumentException("Email is already in use!");
        }

        driver.setPassword(passwordEncoder.encode(driver.getPassword()));
        Role driverRole = roleRepository.findByName("ROLE_DRIVER")
                .orElseThrow(() -> new RuntimeException("Error: ROLE_DRIVER not found. Please pre-populate roles."));
        Set<Role> roles = new HashSet<>();
        roles.add(driverRole);
        driver.setRoles(roles);

        return driverRepository.save(driver);
    }

    @Transactional(readOnly = true)
    public Authentication authenticateUser(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
