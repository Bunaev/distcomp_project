package com.publisher.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient discussionWebClient() {
        return WebClient.create("http://localhost:24130/api/v1.0/reactions");
    }
}
