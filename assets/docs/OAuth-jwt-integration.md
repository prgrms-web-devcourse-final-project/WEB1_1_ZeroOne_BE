
# `OAuth 2` 와 `JWT` 통합하기

`OAuth` 를 이용한 소셜 로그인은 남들이 자주 사용하는 것만 봐왔지, 제가 직접 만들어 사용하는 것은 처음이었습니다.

그래서 `OAuth` 의 전반적인 이해가 필요하였고 다음의 영상을 참고해 공부하였습니다.


> <p align="center">
>    <a href="https://www.youtube.com/playlist?list=PLJkjrxxiBSFALedMwcqDw_BPaJ3qqbWeB">
>        <img src="https://img.youtube.com/vi/xsmKOo-sJ3c/0.jpg" width="40%" height="40%">
>    </a>
> <a href="https://www.youtube.com/playlist?list=PLJkjrxxiBSFALedMwcqDw_BPaJ3qqbWeB">
> <br>[개발자 유미] - 스프링 OAuth2 클라이언트 JWT
> </a>
> </p>

이를 통해 소셜 로그인 자체는 성공적으로 구현하였으나, 로그인 후 자체 JWT 를 발급하는 부분이 문제되었습니다. 
`OAuth` 의 경우 프론트에서 `href` 로 인증을 진행하고 성공시 특정 `url` 로 `redirect` 되기 때문입니다.

이를 해결하기 위해 `OAuth` 로그인 성공 시 자체 임시 토큰을 `url` 에 넣어 토큰 발급 `Endpoint` 에 `redirect` 되는 방안을 고안했습니다.

- [`Source code`](https://github.com/jbw9964/Devcourse-Final-Project/blob/develop/src/main/java/com/palettee/global/security/oauth/handler/OAuth2LoginSuccessHandler.java)

   ```java
   @Slf4j
   @Component
   public class OAuth2LoginSuccessHandler
           extends SimpleUrlAuthenticationSuccessHandler {
   
       private final JwtUtils jwtUtils;
       private final String successRedirectUri;
   
       /**
        * 로그인 성공해서 임시 토큰으로 진짜 토큰들 발급하는 {@code /token/issue?token=} API 로 redirect
        */
       @Override
       public void onAuthenticationSuccess(
               HttpServletRequest request, 
               HttpServletResponse response, 
               Authentication authentication
       ) throws IOException, ServletException {
   
           CustomOAuth2User customUserDetail 
                   = (CustomOAuth2User) authentication.getPrincipal();
           String temporaryToken 
                   = jwtUtils.createTemporaryToken(customUserDetail.getUser());
   
           log.info("Temporary token issued in CustomLoginSuccessHandler - token: {}", temporaryToken);
   
           // 임시 토큰으로 access, refresh 토큰 발급하는 endpoint 로 redirect
           // local profile 은 back 으로, deploy 는 프론트 주소로
           response.sendRedirect(successRedirectUri + "?token=" + temporaryToken);
       }
   }
   ```

이를 통해 `OAuth` 인증의 강점은 살리면서 `JWT` 통합을 성공적으로 완료할 수 있었습니다.

---