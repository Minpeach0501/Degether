package com.hanghae.degether.sse;

import com.hanghae.degether.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Notification findByIdAndReceiver(Long notificationId, User user);

    List<Notification> findAllByReceiver(User user);
}
