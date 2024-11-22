package com.palettee.global.security.dto;

/*
참고 : https://developers.google.com/identity/openid-connect/openid-connect?hl=ko
scope : profile, email

Attributes 형태 :
{
    sub=?????,
    name=정준상, given_name=준상, family_name=정,
    picture=https://lh3.googleusercontent.com/a/ACg8ocKBMIGxmwdLOO_EYcZhIm1XQ3I4ucyQ4bAlRIuMhDw6ApLGMYf2=s96-c,
    email=jbw9964@gmail.com,
    email_verified=true
}
*/

import java.util.*;
import lombok.*;

/**
 * Google OAuth 용 정보 변환 {@code DTO}
 */
@RequiredArgsConstructor
public class GoogleResponse
        extends OAuth2Response {

    private final Map<String, Object> attributes;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProvider() {
        return "google";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProviderId() {
        return attributes.get("sub").toString();
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
        return attributes.get("picture").toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return attributes.get("name").toString();
    }
}
