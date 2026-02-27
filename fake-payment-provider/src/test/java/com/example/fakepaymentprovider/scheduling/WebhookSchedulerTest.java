package com.example.fakepaymentprovider.scheduling;

import com.example.fakepaymentprovider.config.Container;
import com.example.fakepaymentprovider.entity.Payout;
import com.example.fakepaymentprovider.repository.MerchantRepository;
import com.example.fakepaymentprovider.repository.PayoutRepository;
import com.example.fakepaymentprovider.repository.WebhookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@Testcontainers
@SpringBootTest
class WebhookSchedulerTest extends Container {

    @Autowired
    WebhookScheduler webhookScheduler;

    @Autowired
    PayoutRepository payoutRepository;

    @Autowired
    WebhookRepository webhookRepository;
    @Autowired
    MerchantRepository merchantRepository;

    UUID payoutId;

    static {
        startAll();
    }

    @BeforeEach
    void setup() {
        webhookRepository.deleteAll();
        payoutRepository.deleteAll();

        Payout payout = new Payout();
        payout.setStatus("SUCCESS");
        payout.setAmount(BigDecimal.valueOf(100));
        payout.setMerchantId(merchantRepository.findAll().get(0).getId());
        payout.setCurrency("RUB");

        payoutRepository.save(payout);
        payoutId = payout.getId();
    }

    @Test
    @DisplayName("scheduler sends webhook and persists record")
    void shouldSendWebhook() {

        // when
        webhookScheduler.runOnce();

        // then — webhook persisted
        var webhooks = webhookRepository.findAll();

        assertEquals(1, webhooks.size());
        assertEquals("payout", webhooks.get(0).getEventType());
        assertEquals(payoutId, webhooks.get(0).getEntityId());
    }
}
