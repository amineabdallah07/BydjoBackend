-- V5: QR Code Order Items
-- Each QR product order gets its own QR code linked to customer content

CREATE TABLE qr_order_items (
    id BIGSERIAL PRIMARY KEY,
    order_item_id BIGINT NOT NULL REFERENCES order_items(id) ON DELETE CASCADE,
    qr_type VARCHAR(10) NOT NULL CHECK (qr_type IN ('PHOTO', 'LINK')),
    content TEXT NOT NULL,
    qr_code VARCHAR(36) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_qr_order_items_qr_code ON qr_order_items(qr_code);
CREATE INDEX idx_qr_order_items_order_item_id ON qr_order_items(order_item_id);
