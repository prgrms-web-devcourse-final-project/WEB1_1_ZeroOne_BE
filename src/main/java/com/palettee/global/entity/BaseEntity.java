package com.palettee.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {

    @Column(updatable = false)
    @CreatedDate
    private LocalDateTime createAt;

    @LastModifiedDate
    private LocalDateTime lastModifiedAt;

    // BaseEntity 를 상속 받는 엔티티 위에 아래 어노테이션을 설정해주면 됩니다.
    // @Where(clause = "deleted_at is null")
    private LocalDateTime deletedAt;

    public void deleteSoftly() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isSoftDeleted() {
        return null != deletedAt;
    }

    public void undoDeletion(){
        this.deletedAt = null;
    }
}
