package com.palettee.notification.controller;

import com.palettee.global.security.validation.UserUtils;
import com.palettee.notification.controller.dto.NotificationListResponse;
import com.palettee.notification.controller.dto.NotificationRequest;
import com.palettee.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 클라이언트가 서버를 구독하
    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    public SseEmitter subscribe(@RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {
        return notificationService.subscribe(UserUtils.getContextUser(), lastEventId);
    }

    // 삭제 예정
    @PostMapping("/test-noti")
    public void testNOti(@RequestBody NotificationRequest notificationRequest) {
        notificationService.send(notificationRequest);
    }

    @GetMapping
    public NotificationListResponse getNotifications() {
        return notificationService.getNotifications(UserUtils.getContextUser());
    }

    @PatchMapping("/{notificationId}")
    public void readNotification(@PathVariable("notificationId") Long notificationId) {
        notificationService.readNotification(notificationId, UserUtils.getContextUser());
    }

    @DeleteMapping("/{notificationId}")
    public void deleteNotification(@PathVariable("notificationId") Long notificationId) {
        notificationService.deleteNotification(notificationId, UserUtils.getContextUser());
    }

}
