package com.bydjo.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FIX Bug #13: In-memory token blacklist so revoked JWTs can no longer be used.
 * Tokens are automatically removed once they would have expired anyway.
 */
@Service
public class TokenBlacklistService {

    // token → expiry time in millis
    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    public void blacklist(String token, long expiryMillis) {
        blacklist.put(token, expiryMillis);
    }

    public boolean isBlacklisted(String token) {
        return blacklist.containsKey(token);
    }

    /** Purge tokens that have already expired naturally — runs every hour. */
    @Scheduled(fixedRate = 3_600_000)
    public void purgeExpired() {
        long now = System.currentTimeMillis();
        blacklist.entrySet().removeIf(e -> e.getValue() < now);
    }
}
