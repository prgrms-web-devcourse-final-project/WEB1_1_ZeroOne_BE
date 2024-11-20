package com.palettee.global.security.oauth;

import com.palettee.global.security.oauth.handler.*;
import com.palettee.user.domain.*;
import java.util.*;
import lombok.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;
import org.springframework.security.oauth2.core.user.*;

@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final User user;

    /**
     * 어차피 안 쓸거라 상관 없을 듯...?
     */
    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(
                new SimpleGrantedAuthority(
                        user.getUserRole().toString())
        );

        return authorities;
    }

    /**
     * {@link OAuth2LoginSuccessHandler} 에서 로그인 성공 시, 이 메서드로 로그인된 정보 가져올 수 있음.
     */
    @Override
    public String getName() {
        return user.getName();
    }

    /**
     * 로그인 성공 시, {@link OAuth2LoginSuccessHandler} 에서 유저 정보 가져오기 위한 메서드
     */
    public User getUser() {
        return user;
    }

}
