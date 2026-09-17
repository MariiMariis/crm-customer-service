package com.pb.notification.notification;

import com.pb.notification.notification.dto.NotificationRequest;
import com.pb.notification.notification.dto.NotificationResponse;
import com.pb.notification.notification.dto.NotificationStatsResponse;

import java.util.List;

public interface NotificationService {

    NotificationResponse create(NotificationRequest request);

    NotificationResponse findById(Long id);

    List<NotificationResponse> search(NotificationFilter filter);

    List<NotificationResponse> findByTicket(Long ticketId);

    NotificationResponse dispatch(Long id);

    int dispatchPending();

    NotificationResponse markRead(Long id);

    void delete(Long id);

    NotificationStatsResponse stats();
}
