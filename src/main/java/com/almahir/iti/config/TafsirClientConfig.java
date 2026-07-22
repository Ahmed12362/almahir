package com.almahir.iti.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class TafsirClientConfig {

    @Value("${tafsir.api.base-url}")
    private String tafsirBaseUrl;

    @Bean
    public RestClient tafsirRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl(tafsirBaseUrl)
                .build();
    }
}