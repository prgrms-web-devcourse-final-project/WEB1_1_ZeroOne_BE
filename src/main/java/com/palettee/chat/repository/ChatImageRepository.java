package com.palettee.chat.repository;

import com.palettee.chat.domain.*;
import org.springframework.data.jpa.repository.*;

public interface ChatImageRepository
        extends JpaRepository<ChatImage, Long> {

}
