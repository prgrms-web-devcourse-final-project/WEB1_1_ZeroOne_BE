package com.palettee.global.security.dto.oauth;

/**
 * 소셜 미디어별 OAuth2 정보 변환해줄 추상 {@code DTO} 클래스
 */
public abstract class OAuth2Response {

    /**
     * 인증을 담당한 주체
     * <p>
     * > Google, Github, ...
     */
    public abstract String getProvider();

    /**
     * OAuth2 별 사용자의 고유 ID
     */
    public abstract String getProviderId();

    /**
     * OAuth2 로 제공된 사용자 이메일 주소
     */
    public abstract String getEmail();

    /**
     * OAuth2 로 제공된 사용자 사진 주소
     */
    public abstract String getPictureUrl();

    /**
     * OAuth2 로 제공된 사용자 이름
     */
    public abstract String getName();

    /**
     * log 찍을때 편하게 보려고 만듬
     */
    @Override
    public String toString() {
        return String.format(
                "%s={provider=%s, providerId=%s, name=%s, email=%s, pictureUrl=%s}",
                this.getClass().getSimpleName(),
                getProvider(), getProviderId(),
                getName(), getEmail(), getPictureUrl()
        );
    }
}
