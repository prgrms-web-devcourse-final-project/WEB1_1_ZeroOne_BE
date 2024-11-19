package com.palettee.likes.repository;

import com.palettee.likes.domain.*;
import org.springframework.data.jpa.repository.*;

public interface LikeRepository extends JpaRepository<Likes, Long> {
}
