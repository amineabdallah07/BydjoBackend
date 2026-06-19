CREATE TABLE qr_scans (
    id BIGSERIAL PRIMARY KEY,
    qr_code VARCHAR(36) NOT NULL,
    ip_address VARCHAR(45),
    scanned_at TIMESTAMP NOT NULL DEFAULT NOW()
);

ALTER TABLE qr_codes ADD COLUMN scan_count INTEGER NOT NULL DEFAULT 0;

CREATE INDEX idx_qr_scans_qr_code ON qr_scans(qr_code);
