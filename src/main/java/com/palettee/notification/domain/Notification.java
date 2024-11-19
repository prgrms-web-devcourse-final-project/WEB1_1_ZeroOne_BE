package com.palettee.notification.domain;

import jakarta.persistence.*;

@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String title;

    private String content;

    @Enumerated(EnumType.STRING)
    private AlertType type;

    private Boolean isRead;
}
