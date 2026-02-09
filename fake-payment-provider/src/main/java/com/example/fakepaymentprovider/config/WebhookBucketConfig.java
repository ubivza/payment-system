package com.example.fakepaymentprovider.config;

import com.example.fakepaymentprovider.service.MetricsCollector;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketListener;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class WebhookBucketConfig {
    private final MetricsCollector metricsCollector;
    @Value("${rate-limit.webhook.bucket.capacity}")
    private Long capacity;
    @Value("${rate-limit.webhook.bucket.refill-tokens}")
    private Long refillTokens;
    @Value("${rate-limit.webhook.bucket.refill-duration}")
    private Long refillDuration;

    @Bean
    public Bucket webhookBucket() {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(refillTokens, Duration.ofMinutes(refillDuration))
                .build();

        BucketListener listener = new BucketListener() {
            @Override
            public void onConsumed(long tokens) {
                metricsCollector.recordWebhookBucket(true);
            }

            @Override
            public void onRejected(long tokens) {
                metricsCollector.recordWebhookBucket(false);
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
