package com.palettee.user.domain;

import com.palettee.archive.domain.*;
import com.palettee.gathering.domain.*;
import com.palettee.global.entity.*;
import com.palettee.likes.domain.*;
import com.palettee.portfolio.domain.*;
import com.palettee.user.controller.dto.request.users.*;
import com.palettee.user.exception.*;
import jakarta.persistence.*;
import java.util.*;
import lombok.*;

@Entity
@Table(indexes = {
        @Index(name = "idx_email", columnList = "user_email"),
        @Index(name = "idx_oauth_identity", columnList = "oauth_identity"),
        @Index(name = "portFolioMDuplexIndex", columnList = "major_job_Group, minor_job_Group"),   //복합인덱스를 사용하여 majorJobGruop
        @Index(name = "portFolioMinorIndex", columnList = "minor_job_Group")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    /**
     * 사용자의 소셜 로그인 정보를 담는 column
     * <p>
     * > {@code google 12221}, {@code github 443312} 이런 형식
     */
    @Column(name = "oauth_identity", length = 50, unique = true)
    private String oauthIdentity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole userRole;

    @Column(name = "user_email", unique = true, nullable = false)
    private String email;

    @Column(name = "user_image_url")
    private String imageUrl;

    @Column(name = "user_name", length = 50)
    private String name;

    @Column(name = "brief_intro", length = 100)
    private String briefIntro;

    @Column(name = "job_title", length = 100)
    private String jobTitle;

    @Enumerated(EnumType.STRING)
    private Division division;

    @Enumerated(EnumType.STRING)
    private MajorJobGroup majorJobGroup;

    @Enumerated(EnumType.STRING)
    private MinorJobGroup minorJobGroup;

    /**
     * 유저의 권한을 {@code target} 보다 높거나 같게 변경하는 메서드
     *
     * <li>만약 현재 유저의 권한이 이미 {@code target} 보다 높으면 권한 상승은 이뤄지지 않음</li>
     * <li>또한 {@code ADMIN} 으로 권한 상승은 이뤄지지 않음</li>
     *
     * @param target 상승시킬 권한
     * @see UserRole#upgrade
     */
    public void changeUserRole(UserRole target) {
        this.userRole = UserRole.upgrade(this.userRole, target);
    }

    @Builder
    public User(MinorJobGroup minorJobGroup, MajorJobGroup majorJobGroup, Division division,
            String jobTitle, String briefIntro, String name, String imageUrl, String email,
            UserRole userRole, String oauthIdentity, Long id) {
        this.minorJobGroup = minorJobGroup;
        this.majorJobGroup = majorJobGroup;
        this.division = division;
        this.jobTitle = jobTitle;
        this.briefIntro = briefIntro;
        this.name = name;
        this.imageUrl = imageUrl;
        this.email = email;
        this.userRole = userRole;
        this.oauthIdentity = oauthIdentity;
        this.id = id;
    }

    /**
     * 정보 변경 요청에 따라 {@code User} 정보를 변경하는 메서드
     *
     * @throws InvalidDivisionException  요청의 소속 잘못 주어진 경우
     * @throws InvalidJobGroupException  {@code updateRequest} 의 {@code jobGroup} 이 잘못된 경우
     * @throws JobGroupMismatchException {@code 대직군} 과 {@code 소직군} 이 잘못 이어진 경우
     */
    public User update(RegisterBasicInfoRequest updateRequest)
            throws InvalidJobGroupException, JobGroupMismatchException {

        Division division = Division.of(updateRequest.division());
        MajorJobGroup majorGroup = MajorJobGroup.of(updateRequest.majorJobGroup());
        MinorJobGroup minorGroup = MinorJobGroup.of(updateRequest.minorJobGroup());

        if (!majorGroup.matches(minorGroup)) {
            throw JobGroupMismatchException.EXCEPTION;
        }

        return this.update(updateRequest.name(), updateRequest.briefIntro(),
                updateRequest.imageUrl(), updateRequest.jobTitle(),
                division, majorGroup, minorGroup);
    }

    /**
     * 정보 변경 요청에 따라 {@code User} 정보를 변경하는 메서드
     *
     * @throws InvalidDivisionException  요청의 소속 잘못 주어진 경우
     * @throws InvalidJobGroupException  {@code updateRequest} 의 {@code jobGroup} 이 잘못된 경우
     * @throws JobGroupMismatchException {@code 대직군} 과 {@code 소직군} 이 잘못 이어진 경우
     */
    public User update(EditUserInfoRequest editRequest)
            throws InvalidJobGroupException, JobGroupMismatchException {

        Division division = Division.of(editRequest.division());
        MajorJobGroup majorGroup = MajorJobGroup.of(editRequest.majorJobGroup());
        MinorJobGroup minorGroup = MinorJobGroup.of(editRequest.minorJobGroup());

        if (!majorGroup.matches(minorGroup)) {
            throw JobGroupMismatchException.EXCEPTION;
        }

        return this.update(editRequest.name(), editRequest.briefIntro(),
                editRequest.imageUrl(), editRequest.jobTitle(),
                division, majorGroup, minorGroup);
    }

    /**
     * 정보 바꾸는 내부 메서드
     */
    private User update(
            String name, String intro, String imageUrl, String jobTitle,
            Division division, MajorJobGroup majorGroup, MinorJobGroup minorGroup
    ) {
        this.name = name;
        this.briefIntro = intro;
        this.imageUrl = imageUrl;
        this.jobTitle = jobTitle;
        this.division = division;
        this.majorJobGroup = majorGroup;
        this.minorJobGroup = minorGroup;

        return this;
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private final List<Gathering> gatherings = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private final List<PortFolio> portfolios = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private final List<Likes> likes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private final List<RelatedLink> relatedLinks = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private final List<Archive> archives = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private final List<StoredProfileImageUrl> storedProfileImageUrls = new ArrayList<>();

    @Builder
    public User(
            String oauthIdentity, UserRole userRole,
            String email, String imageUrl,
            String name, String briefIntro
    ) {
        this.oauthIdentity = oauthIdentity;
        this.userRole = userRole;
        this.email = email;
        this.imageUrl = imageUrl;
        this.name = name;
        this.briefIntro = briefIntro;
    }

    public void addGathering(Gathering gathering) {
        this.gatherings.add(gathering);
    }

    public void addPortfolio(PortFolio portfolio) {
        this.portfolios.add(portfolio);
    }

    public void addLike(Likes likes) {
        this.likes.add(likes);
    }

    public void addRelatedLink(RelatedLink relatedLink) {
        this.relatedLinks.add(relatedLink);
    }

    public void addArchive(Archive archive) {
        this.archives.add(archive);
    }

    public void addStoredProfileImageUrl(StoredProfileImageUrl storedProfileImageUrl) {
        this.storedProfileImageUrls.add(storedProfileImageUrl);
    }

    public User(String email, String imageUrl, String name, String briefIntro, MajorJobGroup majorJobGroup, MinorJobGroup minorJobGroup) {
        this.email = email;
        this.imageUrl = imageUrl;
        this.name = name;
        this.briefIntro = briefIntro;
        this.majorJobGroup = majorJobGroup;
        this.minorJobGroup = minorJobGroup;
    }
}
