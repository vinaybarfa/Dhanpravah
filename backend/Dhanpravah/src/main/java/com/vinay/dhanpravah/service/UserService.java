package com.vinay.dhanpravah.service;

import com.vinay.dhanpravah.domain.AppUser;
import com.vinay.dhanpravah.domain.UserRole;
import com.vinay.dhanpravah.dto.*;
import com.vinay.dhanpravah.exception.BusinessRuleException;
import com.vinay.dhanpravah.exception.NotFoundException;
import com.vinay.dhanpravah.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class UserService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final UserRepository users;
    private final NotificationService notifications;
    private final JwtService jwtService;

    @Transactional
    public void changePassword(Long userId, @Valid ChangePasswordRequest request) {
        AppUser user =
                users.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getPasswordHash() != null
                && !matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessRuleException("Current password is incorrect");
        }
        user.updatePassword(hash(request.newPassword()));
        users.save(user);
    }

    private static boolean matches(String password, String stored) {
        try {
            String[] parts = stored.split("\\$", -1);
            return parts.length == 2
                    && MessageDigest.isEqual(
                    Base64.getDecoder().decode(parts[1]),
                    digest(password, Base64.getDecoder().decode(parts[0]))
            );
        } catch (IllegalArgumentException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            return false;
        }
    }

    @Transactional
    public UserResponse update(Long userId, @Valid UpdateUserRequest request) {
        AppUser user =
                users.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        users
                .findByEmailIgnoreCase(request.email())
                .ifPresent(
                        existing -> {
                            if (!existing.getId().equals(userId))
                                throw new BusinessRuleException("Email address is already registered");
                        });
        user.updateProfile(request.fullName().trim(), request.email().trim());
        return UserResponse.from(users.save(user));
    }

    @Transactional
    public void resetPassword(@Valid ResetPasswordRequest request) {
        String email = request.email().trim();
        String mobile = request.mobileNumber().trim();
        AppUser user =
                users
                        .findByEmailIgnoreCase(email)
                        .orElseThrow(() -> new BusinessRuleException("Email address is not registered"));
        if (!Objects.equals(user.getMobileNumber(), mobile)) {
            throw new BusinessRuleException("Mobile number does not match this email address");
        }

        user.updatePassword(hash(request.newPassword()));
        users.save(user);
    }

    private static String hash(String password) {
        try {
            byte[] salt = new byte[16];
            RANDOM.nextBytes(salt);
            byte[] digest = digest(password, salt);
            return Base64.getEncoder().encodeToString(salt)
                    + "$"
                    + Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalArgumentException("Password hashing is unavailable", e);
        }
    }

    private static byte[] digest(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec key = new PBEKeySpec(password.toCharArray(), salt, 120_000, 256);
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(key).getEncoded();
    }

    @Transactional
    public AuthResponse authenticate(@Valid LoginRequest request) {
        AppUser user =
                users
                        .findByEmailIgnoreCase(request.email())
                        .orElseThrow(() -> new NotFoundException("Invalid email or password"));

        if (user.getPasswordHash() != null && !matches(request.password(), user.getPasswordHash())) {
            throw new BusinessRuleException("Invalid email or password");
        }

        notifications.loginAlert(user);
        return new AuthResponse(jwtService.issue(user), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public UserResponse findByEmail(String email) {
        return UserResponse.from(
                users
                        .findByEmailIgnoreCase(email)
                        .orElseThrow(
                                () -> new NotFoundException("User not found")));
    }


    public UserResponse create(@Valid CreateUserRequest request) {
        if (users.existsByEmail(request.email()))
            throw new BusinessRuleException("Email address is already registered");

        if (users.existsByMobileNumber(request.mobileNumber()))
            throw new BusinessRuleException("Mobile number is already registered");

        AppUser user = AppUser.builder()
                .fullName(request.fullName())
                .email(request.email())
                .mobileNumber(request.mobileNumber())
                .role(request.role() == null ? UserRole.CUSTOMER : request.role())
                .build();

        if (request.password() != null && !request.password().isBlank()) {
            user.updatePassword(hash(request.password()));
        }
        return UserResponse.from(users.save(user));
    }

}
