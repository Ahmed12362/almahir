package com.almahir.iti.service.impl;

import com.almahir.iti.service.AgoraService;
import io.agora.media.RtcTokenBuilder2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AgoraServiceImpl implements AgoraService {
    @Value("${agora.app-id}")
    private String appId;

    @Value("${agora.app-certificate}")
    private String appCertificate;

    @Value("${agora.expiration-time-in-seconds:3600}")
    private int expirationTimeInSeconds;

    @Override
    public String generateToken(String channelName, UUID userUuid) {
        RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();
        String userAccount = userUuid.toString();
        RtcTokenBuilder2.Role role = RtcTokenBuilder2.Role.ROLE_PUBLISHER;

        int tokenExpiration = expirationTimeInSeconds;
        int privilegeExpiration = expirationTimeInSeconds;

        return tokenBuilder.buildTokenWithUserAccount(
                appId,
                appCertificate,
                channelName,
                userAccount,
                role,
                tokenExpiration,
                privilegeExpiration
        );
    }
}
