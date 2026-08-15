package com.almahir.iti.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class PaymobProperties {
    private final String baseUrl;
    private final String secretKey;
    private final String publicKey;
    private final String hmacSecret;
    private final Integer cardIntegrationId;
    private final Integer walletIntegrationId;
    private String notificationUrl;
}