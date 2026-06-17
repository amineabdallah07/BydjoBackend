CREATE TABLE qr_codes (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(36) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'FREE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_at TIMESTAMP,
    customer_name VARCHAR(255),
    product_name VARCHAR(255),
    order_number VARCHAR(30),
    qr_type VARCHAR(10),
    content TEXT
);

CREATE INDEX idx_qr_codes_status ON qr_codes(status);
CREATE INDEX idx_qr_codes_code ON qr_codes(code);
