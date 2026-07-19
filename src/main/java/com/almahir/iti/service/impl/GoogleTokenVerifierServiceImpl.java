package com.almahir.iti.service.impl;

import com.almahir.iti.exception.InvalidGoogleTokenException;
import com.almahir.iti.service.GoogleTokenVerifierService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleTokenVerifierServiceImpl implements GoogleTokenVerifierService {

    @Value("${google.oauth.client-ids}")
    private String googleClientIds;

    @Override
    public GoogleIdToken.Payload verify(String idToken) {
        List<String> clientIds = Arrays.stream(googleClientIds.split(","))
                .map(String::trim)
                .filter(clientId -> !clientId.isBlank())
                .toList();

        if (clientIds.isEmpty()) {
            throw new InvalidGoogleTokenException("Google client ID is not configured");
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(clientIds)
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);

            if (googleIdToken == null) {
                throw new InvalidGoogleTokenException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
                throw new InvalidGoogleTokenException("Google email is not verified");
            }

            return payload;
        } catch (GeneralSecurityException | IOException | IllegalArgumentException ex) {
            log.debug("Google ID token verification failed", ex);
            throw new InvalidGoogleTokenException("Unable to verify Google ID token");
        }
    }
}
