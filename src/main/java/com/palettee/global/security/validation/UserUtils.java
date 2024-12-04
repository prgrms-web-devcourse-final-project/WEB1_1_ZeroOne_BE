package com.palettee.global.security.validation;

import com.palettee.global.exception.PaletteException;
import com.palettee.global.security.oauth.CustomOAuth2User;
import com.palettee.user.domain.User;
import com.palettee.user.exception.UserNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.webjars.NotFoundException;

public class UserUtils {

    public static User getContextUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(authentication == null);
        System.out.println(!(authentication.getPrincipal() instanceof CustomOAuth2User));
        // 인증 정보가 없으면 예외를 던집니다.
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomOAuth2User)) {
            throw UserNotFoundException.EXCEPTION;
        }

        // 인증된 사용자 정보를 CustomOAuth2User로 캐스팅하여 User 객체를 반환합니다.
        return ((CustomOAuth2User) authentication.getPrincipal()).getUser();
    }
}

