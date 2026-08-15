package com.almahir.iti.client;

import com.almahir.iti.config.PaymobProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymobHmacVerifier {

    private static final String HMAC_ALGO = "HmacSHA512";

    private final PaymobProperties paymobProperties;

    public boolean isValid(Map<String, Object> obj, String receivedHmac) {
        if (receivedHmac == null || receivedHmac.isBlank()) {
            log.warn("Webhook received without hmac query param.");
            return false;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> order = (Map<String, Object>) obj.get("order");
        @SuppressWarnings("unchecked")
        Map<String, Object> sourceData = (Map<String, Object>) obj.get("source_data");

        StringBuilder concatenated = new StringBuilder();
        concatenated.append(str(obj.get("amount_cents")));
        concatenated.append(str(obj.get("created_at")));
        concatenated.append(str(obj.get("currency")));
        concatenated.append(str(obj.get("error_occured")));
        concatenated.append(str(obj.get("has_parent_transaction")));
        concatenated.append(str(obj.get("id")));
        concatenated.append(str(obj.get("integration_id")));
        concatenated.append(str(obj.get("is_3d_secure")));
        concatenated.append(str(obj.get("is_auth")));
        concatenated.append(str(obj.get("is_capture")));
        concatenated.append(str(obj.get("is_refunded")));
        concatenated.append(str(obj.get("is_standalone_payment")));
        concatenated.append(str(obj.get("is_voided")));
        concatenated.append(str(order != null ? order.get("id") : null));
        concatenated.append(str(obj.get("owner")));
        concatenated.append(str(obj.get("pending")));
        concatenated.append(str(sourceData != null ? sourceData.get("pan") : null));
        concatenated.append(str(sourceData != null ? sourceData.get("sub_type") : null));
        concatenated.append(str(sourceData != null ? sourceData.get("type") : null));
        concatenated.append(str(obj.get("success")));

        String computed = computeHmacSha512(concatenated.toString(), paymobProperties.getHmacSecret());

        boolean matches = computed.equalsIgnoreCase(receivedHmac);
        if (!matches) {
            log.warn("HMAC mismatch. computed={}, received={}", computed, receivedHmac);
        }
        return matches;
    }

    private String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String computeHmacSha512(String data, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(rawHmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to compute HMAC", e);
        }
    }
}