package com.almahir.iti.service.impl;

import com.almahir.iti.client.PaymobClient;
import com.almahir.iti.client.PaymobHmacVerifier;
import com.almahir.iti.config.PaymobProperties;
import com.almahir.iti.dto.request.CreateIntentionRequest;
import com.almahir.iti.dto.response.CreateIntentionResponse;
import com.almahir.iti.dto.response.PaymentStatusResponse;
import com.almahir.iti.dto.response.PaymobIntentionResponse;
import com.almahir.iti.exception.*;
import com.almahir.iti.model.*;
import com.almahir.iti.model.enums.PaymentMethod;
import com.almahir.iti.model.enums.PaymentStatus;
import com.almahir.iti.repository.PaymentTransactionRepository;
import com.almahir.iti.repository.SubscriptionPackageRepository;
import com.almahir.iti.repository.UserSubscriptionRepository;
import com.almahir.iti.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final SubscriptionPackageRepository packageRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PaymobClient paymobClient;
    private final PaymobProperties paymobProperties;
    private final PaymobHmacVerifier paymobHmacVerifier;

    private static final Duration INTENTION_TTL = Duration.ofMinutes(15);

    @Override
    @Transactional
    public CreateIntentionResponse createIntention(User user, CreateIntentionRequest request) {

        SubscriptionPackage pkg = packageRepository.findByCodeAndActiveTrue(request.packageId())
                .orElseThrow(() -> new InvalidPackageException(
                        "Package not found or not purchasable: " + request.packageId()));

        PaymentMethod method = parseMethod(request.method());

        boolean alreadyActive = userSubscriptionRepository.findByUserId(user.getId())
                .filter(sub -> sub.getSubscriptionPackage().getId().equals(pkg.getId()))
                .filter(sub -> sub.getExpiresAt() != null && sub.getExpiresAt().isAfter(Instant.now()))
                .isPresent();
        if (alreadyActive) {
            throw new ConflictException("You already have this package active.");
        }

        String idempotencyKey = request.idempotencyKey();

        Optional<PaymentTransaction> lastAttempt = transactionRepository
                .findTopByUserIdAndIdempotencyKeyOrderByCreatedAtDesc(user.getId(), idempotencyKey);

        if (lastAttempt.isPresent()) {
            PaymentTransaction last = lastAttempt.get();

            if (last.getStatus() == PaymentStatus.SUCCESS) {
                throw new ConflictException("This payment was already completed.");
            }

            boolean stillValid = last.getStatus() == PaymentStatus.PENDING
                    && last.getIntentionExpiresAt() != null
                    && last.getIntentionExpiresAt().isAfter(Instant.now());

            if (stillValid) {
                log.info("Reusing valid pending intention for idempotencyKey {}", idempotencyKey);
                return toResponse(last);
            }
        }

        long attemptNumber = transactionRepository.countByUserIdAndIdempotencyKey(user.getId(), idempotencyKey);
        String specialReference = user.getId() + ":" + idempotencyKey
                + (attemptNumber > 0 ? ":r" + attemptNumber : "");

        int integrationId = (method == PaymentMethod.CARD)
                ? paymobProperties.getCardIntegrationId()
                : paymobProperties.getWalletIntegrationId();

        PaymobIntentionResponse paymobResponse;
        try {
            paymobResponse = paymobClient.createIntention(
                    pkg.getPriceMinorUnits(),
                    pkg.getCurrencyCode(),
                    integrationId,
                    specialReference,
                    buildBillingData(user)
            );
        } catch (IntentionAlreadyExistsException e) {
            // حالة نادرة: Paymob بتقول موجودة بس مش لاقيينها عندنا (مثلاً كتابة سابقة فشلت تتسجل)
            return transactionRepository.findByPaymobIntentionId(specialReference)
                    .map(this::toResponse)
                    .orElseThrow(() -> new PaymobUnavailableException(
                            "Paymob reports this reference already exists, but we have no matching record. Please contact support."));
        }

        Instant expiresAt = Instant.now().plus(INTENTION_TTL);

        PaymentTransaction transaction = PaymentTransaction.builder()
                .user(user)
                .subscriptionPackage(pkg)
                .paymobIntentionId(specialReference)
                .idempotencyKey(idempotencyKey)
                .paymobClientSecret(paymobResponse.client_secret())
                .method(method)
                .status(PaymentStatus.PENDING)
                .amountMinorUnits(pkg.getPriceMinorUnits())
                .currencyCode(pkg.getCurrencyCode())
                .intentionExpiresAt(expiresAt)
                .build();

        transactionRepository.save(transaction);

        return toResponse(transaction);
    }

    @Override
    public PaymentStatusResponse getStatus(User user, String intentionId) {
        PaymentTransaction tx = transactionRepository.findByPaymobIntentionId(intentionId)
                .orElseThrow(() -> new ResourceNotFoundException("Intention not found: " + intentionId));

        if (!tx.getUser().getId().equals(user.getId())) {
            throw new ForbiddenOperationException("This intention does not belong to you.");
        }

        return new PaymentStatusResponse(
                tx.getStatus().name(),
                tx.getPaymobTransactionId(),
                tx.getFailureReasonCode()
        );
    }

    @Override
    @Transactional
    public void handlePaymobWebhook(Map<String, Object> payload, String hmac) {
        @SuppressWarnings("unchecked")
        Map<String, Object> obj = (Map<String, Object>) payload.get("obj");
        if (obj == null) {
            log.warn("Webhook payload missing 'obj' key, ignoring.");
            return;
        }

        if (!paymobHmacVerifier.isValid(obj, hmac)) {
            log.warn("Webhook rejected: invalid HMAC signature.");
            throw new InvalidHmacException("Invalid webhook signature.");
        }

        String paymobTransactionId = String.valueOf(obj.get("id"));
        boolean success = Boolean.TRUE.equals(obj.get("success"));

        @SuppressWarnings("unchecked")
        Map<String, Object> order = (Map<String, Object>) obj.get("order");
        String specialReference = order != null ? String.valueOf(order.get("merchant_order_id")) : null;

        PaymentTransaction tx = transactionRepository.findByPaymobIntentionId(specialReference)
                .orElse(null);
        if (tx == null) {
            log.warn("Webhook for unknown intention reference: {}", specialReference);
            return;
        }

        if (tx.getStatus() != PaymentStatus.PENDING) {
            log.info("Webhook for already-processed transaction {}, ignoring.", paymobTransactionId);
            return;
        }

        if (transactionRepository.existsByPaymobTransactionId(paymobTransactionId)) {
            log.info("Duplicate webhook delivery for transaction {}, ignoring.", paymobTransactionId);
            return;
        }

        tx.setPaymobTransactionId(paymobTransactionId);
        tx.setStatus(success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        if (!success) {
            tx.setFailureReasonCode(String.valueOf(obj.getOrDefault("txn_response_code", "UNKNOWN")));
        }
        transactionRepository.save(tx);

        if (success) {
            activateSubscription(tx);
        }
    }

    private void activateSubscription(PaymentTransaction tx) {
        User user = tx.getUser();
        SubscriptionPackage pkg = tx.getSubscriptionPackage();

        UserSubscription sub = userSubscriptionRepository.findByUserId(user.getId())
                .orElse(UserSubscription.builder().user(user).build());

        Instant now = Instant.now();
        sub.setSubscriptionPackage(pkg);
        sub.setStartedAt(now);
        sub.setExpiresAt(now.plus(Duration.ofDays(pkg.getDurationDays())));

        userSubscriptionRepository.save(sub);
        log.info("Activated subscription for user {} with package {}", user.getId(), pkg.getCode());
    }

    private PaymentMethod parseMethod(String raw) {
        try {
            return PaymentMethod.valueOf(raw);
        } catch (IllegalArgumentException e) {
            throw new InvalidPackageException("Invalid payment method: " + raw);
        }
    }

    private Map<String, Object> buildBillingData(User user) {
        return Map.ofEntries(
                Map.entry("first_name", user.getFirstName()),
                Map.entry("last_name", user.getLastName()),
                Map.entry("email", user.getEmail()),
                Map.entry("phone_number", user.getPhoneNumber() != null ? user.getPhoneNumber() : "+201000000000"),
                Map.entry("apartment", "NA"),
                Map.entry("floor", "NA"),
                Map.entry("street", "NA"),
                Map.entry("building", "NA"),
                Map.entry("city", "NA"),
                Map.entry("state", "NA"),
                Map.entry("country", "EG")
        );
    }

    private CreateIntentionResponse toResponse(PaymentTransaction tx) {
        return new CreateIntentionResponse(
                tx.getPaymobIntentionId(),
                tx.getPaymobClientSecret(),
                paymobProperties.getPublicKey(),
                tx.getAmountMinorUnits(),
                tx.getCurrencyCode(),
                tx.getIntentionExpiresAt()
        );
    }
}