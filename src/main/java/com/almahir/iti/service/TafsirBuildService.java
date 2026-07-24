package com.almahir.iti.service;

import java.util.concurrent.CompletableFuture;

public interface TafsirBuildService {

    CompletableFuture<Void> buildFullTafsirAsync(String tafsirKey, String language);
}