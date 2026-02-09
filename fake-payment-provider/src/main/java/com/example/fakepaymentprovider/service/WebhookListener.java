package com.example.fakepaymentprovider.service;

import com.example.fake.dto.StatusUpdate;

public interface WebhookListener {
    void updateStatus(StatusUpdate statusUpdate);
}
