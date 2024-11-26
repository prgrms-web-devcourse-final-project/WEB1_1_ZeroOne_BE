package com.palettee.user.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.*;
import java.util.*;
import org.hibernate.validator.constraints.*;

public record RegisterBasicInfoRequest(

        @NotBlank(message = "이름을 입력해 주세요.")
        @Length(min = 1, max = 50, message = "이름은 최대 50자 까지 입니다.")
        String name,

        @NotBlank(message = "자기소개를 입력해 주세요.")
        @Length(min = 1, max = 100, message = "자기소개는 최대 100자 까지 가능합니다.")
        String briefIntro,

        @NotBlank(message = "직군을 입력해 주세요.")
        String majorJobGroup,

        @NotBlank(message = "직군을 입력해 주세요.")
        String minorJobGroup,

        @NotBlank(message = "직무 타이틀을 입력해 주세요.")
        String jobTitle,

        @NotBlank(message = "소속을 입력해 주세요.")
        String division,

        @Size(max = 5, message = "연관 링크는 최대 5개 까지 가능합니다.")
        List<String> url
) {

}
