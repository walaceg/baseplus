package com.baseplus.modules.auth.service;

import java.time.OffsetDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RefreshTokenCleanupJob {

    private static final long RETENTION_DAYS_AFTER_EXPIRATION = 30;

    private final RefreshTokenService refreshTokenService;

    public RefreshTokenCleanupJob(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    @Scheduled(fixedDelay = 3_600_000L, initialDelay = 3_600_000L)
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenService.purgeExpiredBefore(OffsetDateTime.now().minusDays(RETENTION_DAYS_AFTER_EXPIRATION));
    }
}
