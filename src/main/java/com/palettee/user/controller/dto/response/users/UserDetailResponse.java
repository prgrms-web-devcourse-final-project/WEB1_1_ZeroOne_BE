package com.palettee.user.controller.dto.response.users;

import com.palettee.archive.domain.*;
import com.palettee.user.domain.*;
import java.util.*;

public record UserDetailResponse(

        String name,
        String email,
        String briefIntro,
        String imageUrl,

        MajorJobGroup majorJobGroup,
        MinorJobGroup minorJobGroup,

        String jobTitle,
        Division division,
        UserRole role,
        String portfolioLink,

        List<String> socials,
        ArchiveType color
) {

    public static UserDetailResponse of(User user, String portfolioLink, List<String> socials,
            ArchiveType color, boolean isMine) {

        return new UserDetailResponse(
                user.getName(), user.getEmail(), user.getBriefIntro(), user.getImageUrl(),
                user.getMajorJobGroup(), user.getMinorJobGroup(), user.getJobTitle(),
                user.getDivision(),
                // 자기꺼면 role 보여주고 아니면 null
                isMine ? user.getUserRole() : null, portfolioLink, socials, color
        );
    }
}
