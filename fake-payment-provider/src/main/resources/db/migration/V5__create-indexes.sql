CREATE INDEX idx_transactions_merchant_date ON payment_provider.transactions (merchant_id, created_at);
CREATE INDEX idx_payouts_merchant_date ON payment_provider.payouts (merchant_id, created_at);
CREATE INDEX idx_webhooks_entity ON payment_provider.webhooks (event_type, entity_id);