package com.palettee.notification.service;

import com.palettee.notification.controller.dto.NotificationDetailResponse;
import com.palettee.notification.controller.dto.NotificationListResponse;
import com.palettee.notification.controller.dto.NotificationRequest;
import com.palettee.notification.domain.AlertType;
import com.palettee.notification.domain.Notification;
import com.palettee.notification.exception.NotMyNotification;
import com.palettee.notification.exception.NotificationNotFound;
import com.palettee.notification.repository.EmitterRepository;
import com.palettee.notification.repository.NotificationRepository;
import com.palettee.user.domain.User;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationService {

    private final EmitterRepository emitterRepository;
    private final NotificationRepository notificationRepository;

    @Value("${sse.timeout}")
    private Long ONE_HOUR;

    public SseEmitter subscribe(User user, String lastEventId) {
        String emitterId = makeTimeIncludeId(user);
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(ONE_HOUR));

        emitter.onCompletion(() -> emitterRepository.deleteById(emitterId, user.getId()));
        emitter.onTimeout(() -> emitterRepository.deleteById(emitterId, user.getId()));

        String eventId = makeTimeIncludeId(user);
        sendToClient(emitter, emitterId, eventId,
                "연결되었습니다. EventStream Created. [userId=" + user.getId() + "]");

        if (!lastEventId.isEmpty()) {
            Map<String, Object> events = emitterRepository.findAllEventCacheStartWithUserId(
                    user.getId() + "_");
            events.entrySet().stream()
                    .filter(entry -> lastEventId.compareTo(entry.getKey()) > 0)
                    .forEach(entry -> sendToClient(emitter, entry.getKey(), entry.getKey(),
                            NotificationDetailResponse.of((Notification) entry.getValue())));
            emitterRepository.deleteAllEventCacheStartWithId(user.getId() + "_");
        }
        return emitter;
    }

    private String makeTimeIncludeId(User user) {
        return user.getId() + "_" + System.currentTimeMillis();
    }

    public void sendToClient(SseEmitter emitter, String emitterId, String eventId, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .name("sse")
                    .id(eventId)
                    .data(data));
        } catch (IOException exception) {
            emitterRepository.deleteById(emitterId, 0L);
            log.error("[SSE ERROR] connection is down - emitter : {} emitterId : {} eventId : {} message : {}", emitter, emitterId, eventId, exception.getMessage());
        }
    }

    @Transactional
    public void send(NotificationRequest request) {
        sendNotification(request, saveNotification(request));
    }

    @Transactional
    public Notification saveNotification(NotificationRequest request) {
        Notification notification = Notification.builder()
                .targetId(request.targetId())
                .title(request.title())
                .content(request.content())
                .chatRoomId(request.chatRoomId())
                .type(AlertType.findByInput(request.type()))
                .build();
        notificationRepository.save(notification);
        return notification;
    }

    @Async
    public void sendNotification(NotificationRequest request, Notification notification) {
        String receiverId = String.valueOf(request.targetId());
        String eventId = receiverId + "_" + System.currentTimeMillis();
        Map<String, SseEmitter> emitters = emitterRepository
                .findAllEmitterStartWithByUserId(receiverId);
        emitters.forEach(
                (key, emitter) -> {
                    emitterRepository.saveEventCache(key, notification);
                    sendToClient(emitter, key, eventId, NotificationDetailResponse.of(notification));
                }
        );
    }

    @Transactional(readOnly = true)
    public NotificationListResponse getNotifications(User user) {
        return NotificationListResponse.of(
                notificationRepository.findAllByTargetId(user.getId()));
    }

    @Transactional
    public void readNotification(Long notificationId, User user) {
        Notification notification = findNotification(notificationId);
        notification.read(user);
        notificationRepository.save(notification);
    }

    @Transactional
    public void deleteNotification(Long notificationId, User user) {
        Notification notification = findNotification(notificationId);
        if (!notification.getTargetId().equals(user.getId())) {
            throw NotMyNotification.EXCEPTION;
        }
        notificationRepository.delete(notification);
    }

    public Notification findNotification(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> NotificationNotFound.EXCEPTION);
    }
}
