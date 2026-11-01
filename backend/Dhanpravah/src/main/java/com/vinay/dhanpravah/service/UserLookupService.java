package com.vinay.dhanpravah.service;

import com.vinay.dhanpravah.domain.AppUser;
import com.vinay.dhanpravah.exception.NotFoundException;
import com.vinay.dhanpravah.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserLookupService {

    private final UserRepository userRepository;

    public AppUser finsUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not Found"));
    }

    public void requireUser(Long userId) {
        if (!userRepository.existsById(userId))
            throw new NotFoundException("User not found");
    }
}
