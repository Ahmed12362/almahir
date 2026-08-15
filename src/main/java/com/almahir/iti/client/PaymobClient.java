package com.almahir.iti.client;

import com.almahir.iti.config.PaymobProperties;
import com.almahir.iti.dto.response.PaymobIntentionResponse;
import com.almahir.iti.exception.IntentionAlreadyExistsException;
import com.almahir.iti.exception.PaymobUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymobClient {

    private final PaymobProperties paymobProperties;

    private RestClient restClient() {
        return RestClient.builder()
                .baseUrl(paymobProperties.getBaseUrl())
                .defaultHeader("Authorization", "Token " + paymobProperties.getSecretKey())
                .build();
    }

    public PaymobIntentionResponse createIntention(
            long amountMinorUnits,
            String currencyCode,
            int integrationId,
            String specialReference,
            Map<String, Object> billingData
    ) {
        Map<String, Object> body = Map.of(
                "amount", amountMinorUnits,
                "currency", currencyCode,
                "payment_methods", List.of(integrationId),
                "special_reference", specialReference,
                "billing_data", billingData,
                "notification_url", paymobProperties.getNotificationUrl()
        );

        try {
            PaymobIntentionResponse response = restClient()
                    .post()
                    .uri("/v1/intention/")
                    .body(body)
                    .retrieve()
                    .body(PaymobIntentionResponse.class);

            if (response == null) {
                throw new PaymobUnavailableException("Paymob returned an empty response.");
            }
            return response;
        } catch (HttpClientErrorException.BadRequest ex) {
            log.warn("Paymob intention already exists for reference {}", specialReference);
            throw new IntentionAlreadyExistsException(specialReference);
        } catch (RestClientException ex) {
            log.error("Paymob create intention call failed", ex);
            throw new PaymobUnavailableException("Could not create payment intention with Paymob.");
        }
    }
}