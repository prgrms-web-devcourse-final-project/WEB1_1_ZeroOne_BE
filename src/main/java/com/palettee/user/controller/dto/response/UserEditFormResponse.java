package com.palettee.user.controller.dto.response;

import com.palettee.user.domain.*;
import java.util.*;

public record UserEditFormResponse(

        String name,
        String email,
        String briefIntro,
        String imageUrl,
        MajorJobGroup majorJobGroup,
        MinorJobGroup minorJobGroup,
        String jobTitle,
        Division division,
        String portfolioLink,

        List<String> socials
) {

    public static UserEditFormResponse of(User user, String portfolioLink, List<String> socials) {
        return new UserEditFormResponse(
                user.getName(), user.getEmail(), user.getBriefIntro(), user.getImageUrl(),
                user.getMajorJobGroup(), user.getMinorJobGroup(), user.getJobTitle(),
                user.getDivision(), portfolioLink, socials
        );
    }
}
