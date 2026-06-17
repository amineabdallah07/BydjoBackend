CREATE TABLE tshirts (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    owner_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    scan_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE tshirt_scans (
    id BIGSERIAL PRIMARY KEY,
    tshirt_code VARCHAR(50) NOT NULL REFERENCES tshirts(code) ON DELETE CASCADE,
    ip_address VARCHAR(45),
    scanned_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tshirts_owner_id ON tshirts(owner_id);
CREATE INDEX idx_tshirt_scans_tshirt_code ON tshirt_scans(tshirt_code);
