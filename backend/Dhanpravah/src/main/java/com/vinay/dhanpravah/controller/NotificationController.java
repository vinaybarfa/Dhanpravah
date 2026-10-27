package com.vinay.dhanpravah.controller;

import com.vinay.dhanpravah.dto.NotificationResponse;
import com.vinay.dhanpravah.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{userId}/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notifications;

    @GetMapping
    @PreAuthorize("#userId.toString() == authentication.name")
    public List<NotificationResponse> list(@PathVariable Long userId) {
        return notifications.list(userId);
    }
}
