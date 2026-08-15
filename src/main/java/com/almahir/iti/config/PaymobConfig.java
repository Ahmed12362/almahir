package com.almahir.iti.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymobConfig {

    @Value("${paymob.base-url}")
    private String baseUrl;

    @Value("${paymob.secret-key}")
    private String secretKey;

    @Value("${paymob.public-key}")
    private String publicKey;

    @Value("${paymob.hmac-secret}")
    private String hmacSecret;

    @Value("${paymob.integration-id.card}")
    private Integer cardIntegrationId;

    @Value("${paymob.integration-id.wallet:0}")
    private Integer walletIntegrationId;
    @Value("${paymob.notification-url}")
    private String notificationUrl;

    @Bean
    public PaymobProperties paymobProperties() {
        return new PaymobProperties(
                baseUrl,
                secretKey,
                publicKey,
                hmacSecret,
                cardIntegrationId,
                walletIntegrationId,
                notificationUrl
        );
    }
}