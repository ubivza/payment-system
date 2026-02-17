package com.example.paymentservice.client;

import com.example.fake.api.PayoutApiClient;
import com.example.fake.dto.PayoutRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FakePaymentProviderClient {
    private final PayoutApiClient client;

    public UUID createPayout(Double amount, String currency) {
        PayoutRequest request = new PayoutRequest();
        request.setAmount(amount);
        request.setCurrency(currency);

        return client.createPayout(request).getBody().getId();
    }
}
