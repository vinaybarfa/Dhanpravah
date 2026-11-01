package com.vinay.dhanpravah.service;

import com.vinay.dhanpravah.domain.AppUser;
import com.vinay.dhanpravah.domain.NotificationChannel;
import com.vinay.dhanpravah.domain.NotificationStatus;
import com.vinay.dhanpravah.domain.UserNotification;
import com.vinay.dhanpravah.dto.NotificationResponse;
import com.vinay.dhanpravah.exception.NotFoundException;
import com.vinay.dhanpravah.repository.NotificationRepository;
import com.vinay.dhanpravah.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notifications;
    private final UserRepository users;

    @Transactional(readOnly = true)
    public List<NotificationResponse> list(Long userId) {
        if (!users.existsById(userId)) throw new NotFoundException("User not found");
        return notifications.findTop50ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional
    public void loginAlert(AppUser user) {
        send(user, "security", "New sign-in", "Your Dhanpravah Account was signed in successfully.");
    }

    @Transactional
    public void transferAlert(AppUser user, String title, String message) {
        send(user, "transaction", title, message);
    }

    private void send(AppUser user, String type, String title, String message) {
        UserNotification notification = UserNotification.builder()
                .user(user)
                .channel(NotificationChannel.SMS)
                .type(type)
                .title(title)
                .message(message)
                .recipient(user.getMobileNumber())
                .status(NotificationStatus.SENT)
                .build();

        notifications.save(notification);
    }
}
