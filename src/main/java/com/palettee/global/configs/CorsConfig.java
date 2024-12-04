package com.palettee.global.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("http://localhost:3000", "https://palettee22.netlify.app", "https://www.palettee.site"));
        config.addAllowedMethod("*");
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("*"));
        // 클라이언트에서 접근할 수 있도록 노출할 헤더를 명시
        config.setExposedHeaders(List.of("Authorization", "Content-Type", "Cache-Control"));



        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;


    }
}
