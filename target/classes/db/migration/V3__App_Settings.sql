-- V3: App Settings table
-- FIX Bug #14: Persist store settings in DB so admin changes actually save
-- FIX L4: Delivery fees come from DB config, not hardcoded constants

CREATE TABLE app_settings (
    id BIGSERIAL PRIMARY KEY,
    key VARCHAR(100) NOT NULL UNIQUE,
    value TEXT,
    description VARCHAR(300),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed default values (mirror of application.yml defaults)
INSERT INTO app_settings (key, value, description) VALUES
('store.name',               'BY DJO',           'Nom de la boutique'),
('store.phone',              '+216 XX XXX XXX',  'Téléphone de la boutique'),
('store.email',              'contact@bydjo.com','Email de la boutique'),
('store.address',            'Tunis, Tunisia',   'Adresse de la boutique'),
('store.whatsapp',           '+216XXXXXXXX',     'Numéro WhatsApp'),
('store.facebook',           'bydjo',            'Page Facebook (slug)'),
('store.instagram',          'bydjo',            'Compte Instagram'),
('delivery.fee',             '8',                'Frais de livraison standard (TND)'),
('delivery.free_threshold',  '150',              'Montant minimum pour livraison gratuite (TND)')
ON CONFLICT (key) DO NOTHING;
