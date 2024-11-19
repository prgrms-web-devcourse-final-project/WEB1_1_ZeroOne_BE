package com.palettee.notification.repository;

import com.palettee.notification.domain.*;
import org.springframework.data.jpa.repository.*;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

}
