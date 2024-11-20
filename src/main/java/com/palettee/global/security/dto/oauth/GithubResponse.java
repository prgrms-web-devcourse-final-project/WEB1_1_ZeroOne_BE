package com.palettee.global.security.dto.oauth;


import java.util.*;
import lombok.*;

/*
참고 : https://docs.github.com/en/rest/users/users#get-the-authenticated-user
scope : (no_scope) 면 기본적으로 읽을 수 있는 유저 정보들 다 가져옴

Attributes 형태 :
{
    id=????,            // github 유저별 고유한 id
    login=jbw9964,      // github 에 사용되는 유저 이름
    name=청주는사과아님,   // github 에 사용되는 대표(?) 유저 이름
    avatar_url          // 프로필 이미지 url
    =https://avatars.githubusercontent.com/u/110011678?v=4,
    email=jbw9964@gmail.com,
    ... (개많음)
}
*/

/**
 * Github OAuth 용 정보 변환 {@code DTO}
 */
@RequiredArgsConstructor
public class GithubResponse
        extends OAuth2Response {

    /**
     * 사용자 가져오기 위한 Map 객체
     */
    private final Map<String, Object> attributes;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProvider() {
        return "github";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEmail() {
        return attributes.get("email").toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPictureUrl() {
        return attributes.get("avatar_url").toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return attributes.get("name").toString();
    }
}
