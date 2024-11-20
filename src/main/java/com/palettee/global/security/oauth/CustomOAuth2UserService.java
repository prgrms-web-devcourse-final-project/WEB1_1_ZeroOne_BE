package com.palettee.global.security.oauth;

import com.palettee.global.security.dto.*;
import com.palettee.user.domain.*;
import com.palettee.user.repository.*;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.security.oauth2.client.userinfo.*;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.user.*;
import org.springframework.stereotype.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService
        extends DefaultOAuth2UserService {

    private final UserRepository userRepo;
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest
                .getClientRegistration()
                .getRegistrationId();

        // 소셜 로그인 종류에 따라 통일된 형태 (OAuth2Response) 만들기
        Map<String, Object> attributes = oAuth2User.getAttributes();
        OAuth2Response oAuth2Response;

        log.debug("Given attributes: {}", attributes);

        if ("google".equals(registrationId)) {
            log.info("Google OAuth2 attempted.");

            oAuth2Response = new GoogleResponse(attributes);
        } else if ("github".equals(registrationId)) {
            log.info("Github OAuth2 attempted.");

            oAuth2Response = new GithubResponse(attributes);
        } else {
            log.info("Unknown OAuth2 registration id : {}", registrationId);

            // OAuth2LoginFailureHandler 에서 처리할 수 있게
            // OAuth2AuthenticationException 던지기
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("400"),
                    "지원하지 않는 소셜 미디어 입니다.");
        }

        // 소셜 종류에 따른 DTO 완성
        log.debug("oAuth2Response: {}", oAuth2Response);

        // 해당 유저가 처음 로그인 (회원가입) 하는지 확인
        String userOauthIdentity =
                oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();

        // 이전 다른 소셜 계정으로 회원가입 했었는지 확인
        User userFoundByEmail = userRepo.findByEmail(oAuth2Response.getEmail())
                .orElse(null);

        User userFoundByOauth = userRepository.findByOauthIdentity(userOauthIdentity)
                .orElse(null);

        // 처음 회원가입 하는 유저
        if (userFoundByEmail == null && userFoundByOauth == null) {
            log.info("New user trying to signup");
            log.debug("oAuth2Response: {}", oAuth2Response);

            userFoundByEmail = User.builder()
                    .oauthIdentity(userOauthIdentity)
                    .userRole(UserRole.REAL_NEWBIE)
                    .email(oAuth2Response.getEmail())
                    .imageUrl(oAuth2Response.getPictureUrl())
                    .name(oAuth2Response.getName())
                    .build();

            // DB 에 등록 (회원가입)
            userFoundByEmail = userRepo.save(userFoundByEmail);

            log.info("New user were registered successfully.");
        } else if (userFoundByEmail != null && userFoundByOauth == null) {
            log.warn("Current user seems to be registered via different social accounts.");
            log.warn("userFoundByEmail: {}", userFoundByEmail);

            // OAuth2LoginFailureHandler 에서 처리할 수 있게
            // OAuth2AuthenticationException 던지기
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("409"),
                    "동일한 이메일 계정이 존재합니다. 다른 소셜 계정으로 로그인 하세요.");
        }

        return new CustomOAuth2User(userFoundByEmail);
    }
}
