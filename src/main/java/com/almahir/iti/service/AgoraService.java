package com.almahir.iti.service;

import java.util.UUID;

public interface AgoraService {
    public String generateToken(String channelName, UUID userUuid);
}
