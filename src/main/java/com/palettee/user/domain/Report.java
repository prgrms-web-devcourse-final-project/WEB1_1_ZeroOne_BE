package com.palettee.user.domain;

import com.palettee.global.entity.*;
import jakarta.persistence.*;
import java.util.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportType reportType;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(name = "is_fixed", nullable = false)
    private boolean isFixed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "report")
    private final List<ReportComment> reportComments = new ArrayList<>();

    @Builder
    public Report(ReportType reportType, String title, String content, boolean isFixed, User user) {
        this.reportType = reportType;
        this.title = title;
        this.content = content;
        this.isFixed = isFixed;
        this.user = user;
    }

    public void addReportComment(ReportComment reportComment) {
        this.reportComments.add(reportComment);
    }

    public void reportFixed() {
        this.isFixed = true;
    }
}
