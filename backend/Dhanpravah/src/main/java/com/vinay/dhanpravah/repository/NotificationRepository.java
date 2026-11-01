package com.vinay.dhanpravah.repository;

import com.vinay.dhanpravah.domain.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface NotificationRepository extends JpaRepository<UserNotification, UUID> {

    List<UserNotification> findTop50ByUserIdOrderByCreatedAtDesc(Long userId);

}