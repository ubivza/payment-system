CREATE INDEX idx_transactions_merchant_date ON transactions(merchant_id, created_at);
CREATE INDEX idx_payouts_merchant_date ON payouts(merchant_id, created_at);
CREATE INDEX idx_webhooks_entity ON webhooks(event_type, entity_id);