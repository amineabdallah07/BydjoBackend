-- V6: Add is_qr_product column to products table

ALTER TABLE products ADD COLUMN IF NOT EXISTS is_qr_product BOOLEAN NOT NULL DEFAULT FALSE;
