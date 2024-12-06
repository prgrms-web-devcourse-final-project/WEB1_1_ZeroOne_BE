package com.palettee.global.configs;

import net.gpedro.integrations.slack.SlackApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackLogAppenderConfig {

    @Value("${logging.slack.webhook-uri}")
    private String token;

    @Bean
    public SlackApi slackApi() {
        return new SlackApi(token);
    }

}
