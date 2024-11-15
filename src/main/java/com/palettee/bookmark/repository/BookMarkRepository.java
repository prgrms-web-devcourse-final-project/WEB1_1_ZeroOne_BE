package com.palettee.bookmark.repository;

import com.palettee.bookmark.domain.BookMark;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookMarkRepository extends JpaRepository<BookMark, Long> {
}
