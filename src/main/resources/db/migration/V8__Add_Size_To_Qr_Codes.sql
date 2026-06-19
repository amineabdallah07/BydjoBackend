ALTER TABLE qr_codes ADD COLUMN size VARCHAR(10);

CREATE INDEX idx_qr_codes_status_size ON qr_codes(status, size);
