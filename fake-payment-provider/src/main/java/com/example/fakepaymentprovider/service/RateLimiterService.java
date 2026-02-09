package com.example.fakepaymentprovider.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketListener;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RateLimiterService {
    private final MetricsCollector metricsCollector;
    //для распределнной системы меняется на редис/хейзелкаст
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    @Value("${rate-limit.merchant-api.bucket.capacity}")
    private Long capacity;
    @Value("${rate-limit.merchant-api.bucket.refill-tokens}")
    private Long refillTokens;
    @Value("${rate-limit.merchant-api.bucket.refill-duration}")
    private Long refillDuration;

    public boolean tryConsume(String merchantId) {
        Bucket bucket = buckets.computeIfAbsent(merchantId, this::newBucket);

        return bucket.tryConsume(1);
    }

    private Bucket newBucket(String merchantId) {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(refillTokens, Duration.ofMinutes(refillDuration))
                .build();

        BucketListener listener = new BucketListener() {
            @Override
            public void onConsumed(long tokens) {
                metricsCollector.recordMerchantApiBucket(true);
            }

            @Override
            public void onRejected(long tokens) {
                metricsCollector.recordMerchantApiBucket(false);
            }

            @Override
            public void onParked(long nanos) {

            }

            @Override
            public void onInterrupted(InterruptedException e) {

            }

            @Override
            public void onDelayed(long nanos) {

            }
        };

        return Bucket.builder()
                .addLimit(bandwidth)
                .build()
                .toListenable(listener);
    }
}
