package com.aisimulator.backend;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {

    private static final int MAX_REQUESTS_PER_MINUTE = 5;

    // Tracks how many requests each IP has made, and when the counter started
    private final Map<String, RequestWindow> requestCounts = new ConcurrentHashMap<>();

    public boolean isAllowed(String ipAddress) {
        long now = System.currentTimeMillis();

        RequestWindow window = requestCounts.computeIfAbsent(ipAddress, k -> new RequestWindow(now));

        synchronized (window) {
            // If more than 60 seconds passed, reset the counter
            if (now - window.windowStart > 60_000) {
                window.windowStart = now;
                window.count.set(0);
            }

            if (window.count.get() >= MAX_REQUESTS_PER_MINUTE) {
                return false; // blocked
            }

            window.count.incrementAndGet();
            return true; // allowed
        }
    }

    private static class RequestWindow {
        long windowStart;
        AtomicInteger count = new AtomicInteger(0);

        RequestWindow(long start) {
            this.windowStart = start;
        }
    }
}