package com.vinay.dhanpravah.repository;

import com.vinay.dhanpravah.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);

    boolean existsByMobileNumber(String mobileNumber);
}