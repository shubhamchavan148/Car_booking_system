//Handles user profile updates, accessible by the user themselves or by an admin.
package com.example.CBS.controller;

import com.example.CBS.model.User;
import com.example.CBS.payload.request.PasswordUpdateRequest;
import com.example.CBS.security.services.UserDetailsImpl;
import com.example.CBS.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Helper to get current authenticated user's ID
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) authentication.getPrincipal()).getId();
        }
        throw new IllegalStateException("User not authenticated.");
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('RIDER', 'DRIVER', 'ADMIN')")
    public ResponseEntity<User> getCurrentUser() {
        Long userId = getCurrentUserId();
        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('RIDER', 'DRIVER', 'ADMIN')")
    public ResponseEntity<User> updateCurrentUser(@Valid @RequestBody User userDetails) {
        Long userId = getCurrentUserId();
        User updatedUser = userService.updateUserDetails(userId, userDetails);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/me/password")
    @PreAuthorize("hasAnyRole('RIDER', 'DRIVER', 'ADMIN')")
    public ResponseEntity<String> updateCurrentUserPassword(@Valid @RequestBody PasswordUpdateRequest passwordUpdateRequest) {
        Long userId = getCurrentUserId();
        userService.updatePassword(userId, passwordUpdateRequest.getNewPassword());
        return ResponseEntity.ok("Password updated successfully!");
    }


    // Admin-only endpoints for user management
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User userDetails) {
        User updatedUser = userService.updateUserDetails(id, userDetails);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
