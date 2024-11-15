package com.palettee.project.repository;

import com.palettee.project.domain.ProjectImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectImageRepository extends JpaRepository<ProjectImage, Long> {
}
