package com.almahir.iti.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

public interface GoogleTokenVerifierService {
    GoogleIdToken.Payload verify(String idToken);
}
