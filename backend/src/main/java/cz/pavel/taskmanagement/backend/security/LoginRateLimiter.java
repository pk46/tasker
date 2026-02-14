package cz.pavel.taskmanagement.backend.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_DURATION_SECONDS = 300; // 5 minutes

    private final ConcurrentHashMap<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        AttemptInfo info = attempts.get(key);
        if (info == null) return false;

        if (info.isExpired()) {
            attempts.remove(key);
            return false;
        }

        return info.getCount() >= MAX_ATTEMPTS;
    }

    public void recordFailedAttempt(String key) {
        attempts.compute(key, (k, existing) -> {
            if (existing == null || existing.isExpired()) {
                return new AttemptInfo();
            }
            existing.increment();
            return existing;
        });
        log.warn("Failed login attempt for key: {} (count: {})", key, attempts.get(key).getCount());
    }

    public void recordSuccessfulLogin(String key) {
        attempts.remove(key);
    }

    private static class AttemptInfo {
        private final AtomicInteger count = new AtomicInteger(1);
        private final Instant windowStart = Instant.now();

        void increment() {
            count.incrementAndGet();
        }

        int getCount() {
            return count.get();
        }

        boolean isExpired() {
            return Instant.now().isAfter(windowStart.plusSeconds(BLOCK_DURATION_SECONDS));
        }
    }
}
