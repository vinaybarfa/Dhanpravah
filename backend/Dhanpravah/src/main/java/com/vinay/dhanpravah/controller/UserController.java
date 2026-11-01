package com.vinay.dhanpravah.controller;

import com.vinay.dhanpravah.dto.*;
import com.vinay.dhanpravah.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }

    @GetMapping(params = "email")
    public UserResponse byEmail(@RequestParam String email) {
        return userService.findByEmail(email);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.authenticate(request);
    }

    @PostMapping("/password/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
    }

    @PutMapping("/userId")
    @PreAuthorize("#userId.toString() == authentication.name")
    public UserResponse update(
            @PathVariable Long userId, @Valid @RequestBody UpdateUserRequest request
    ) {
        return userService.update(userId, request);
    }

    @PutMapping("/{userId}/password")
    @PreAuthorize("#userId.toString() == authentication.name")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @PathVariable Long userId, @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(userId, request);
    }

}
