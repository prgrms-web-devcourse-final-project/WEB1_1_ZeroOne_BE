package com.palettee.notification.repository;

import com.palettee.notification.domain.*;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("select n from Notification n where n.targetId = :targetId order by n.id desc")
    List<Notification> findAllByTargetId(@Param("targetId") Long targetId);

}
