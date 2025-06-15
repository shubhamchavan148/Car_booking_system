package com.example.CBS.controller;

import com.example.CBS.model.Rider;
import com.example.CBS.model.Driver;
import com.example.CBS.payload.request.LoginRequest;
import com.example.CBS.payload.request.RegisterDriverRequest;
import com.example.CBS.payload.request.RegisterRiderRequest;
import com.example.CBS.payload.response.JwtResponse;
import com.example.CBS.security.jwt.JwtUtils;
import com.example.CBS.security.services.UserDetailsImpl;
import com.example.CBS.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    public AuthController(AuthService authService, JwtUtils jwtUtils) {
        this.authService = authService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/rider/register")
    public ResponseEntity<?> registerRider(@Valid @RequestBody RegisterRiderRequest registerRequest) {
        Rider rider = new Rider(
                registerRequest.getUsername(),
                registerRequest.getPassword(),
                registerRequest.getEmail(),
                registerRequest.getFirstName(),
                registerRequest.getLastName()
        );
        Rider newRider = authService.registerRider(rider);
        return new ResponseEntity<>("Rider registered successfully!", HttpStatus.CREATED);
    }

    @PostMapping("/driver/register")
    public ResponseEntity<?> registerDriver(@Valid @RequestBody RegisterDriverRequest registerRequest) {
        Driver driver = new Driver(
                registerRequest.getUsername(),
                registerRequest.getPassword(),
                registerRequest.getEmail(),
                registerRequest.getFirstName(),
                registerRequest.getLastName(),
                registerRequest.getLicenseNumber()
        );
        Driver newDriver = authService.registerDriver(driver);
        return new ResponseEntity<>("Driver registered successfully!", HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authService.authenticateUser(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        );

        // Generate JWT
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles));
    }
}
