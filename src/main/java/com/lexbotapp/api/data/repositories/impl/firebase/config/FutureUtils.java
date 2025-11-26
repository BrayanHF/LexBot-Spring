package com.lexbotapp.api.data.repositories.impl.firebase.config;

import com.google.api.core.ApiFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.CompletableFuture;

public class FutureUtils {
    public static <T> CompletableFuture<T> toCompletableFuture(ApiFuture<T> apiFuture) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        apiFuture.addListener(
            () -> {
                try {
                    completableFuture.complete(apiFuture.get());
                } catch (Exception e) {
                    completableFuture.completeExceptionally(e);
                }
            },
            MoreExecutors.directExecutor()
        );
        return completableFuture;
    }
}
