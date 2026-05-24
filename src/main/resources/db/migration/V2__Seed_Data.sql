-- Seed data for BY DJO

-- Roles
INSERT INTO roles (name) VALUES ('ADMIN') ON CONFLICT DO NOTHING;
INSERT INTO roles (name) VALUES ('CUSTOMER') ON CONFLICT DO NOTHING;

-- Admin user (phone: +21620000000)
INSERT INTO users (first_name, last_name, phone, email, active, phone_verified)
VALUES ('Admin', 'BYDJO', '+21620000000', 'admin@bydjo.com', TRUE, TRUE)
ON CONFLICT (phone) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.phone = '+21620000000' AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- Sizes
INSERT INTO sizes (name) VALUES ('XS'), ('S'), ('M'), ('L'), ('XL'), ('XXL'), ('38'), ('39'), ('40'), ('41'), ('42'), ('43'), ('44'), ('45'), ('UNIQUE')
ON CONFLICT (name) DO NOTHING;

-- Colors
INSERT INTO colors (name, hex_code) VALUES
('Noir', '#000000'), ('Blanc', '#FFFFFF'), ('Gris', '#808080'),
('Bleu Marine', '#000080'), ('Bleu', '#0000FF'), ('Rouge', '#FF0000'),
('Vert', '#008000'), ('Beige', '#F5F5DC'), ('Rose', '#FFC0CB'),
('Orange', '#FFA500'), ('Jaune', '#FFD700'), ('Marron', '#8B4513')
ON CONFLICT DO NOTHING;

-- Categories
INSERT INTO categories (name, slug, description, active, sort_order) VALUES
('T-Shirts', 't-shirts', 'Premium t-shirts pour homme', TRUE, 1),
('Jeans', 'jeans', 'Jeans de qualité supérieure', TRUE, 2),
('Sneakers', 'sneakers', 'Sneakers tendance', TRUE, 3),
('Caps', 'caps', 'Caps et casquettes stylées', TRUE, 4),
('Vestes', 'vestes', 'Vestes et manteaux premium', TRUE, 5),
('Shorts', 'shorts', 'Shorts confortables', TRUE, 6),
('Accessoires', 'accessoires', 'Accessoires tendance', TRUE, 7)
ON CONFLICT (name) DO NOTHING;

-- Sample Products
INSERT INTO products (name, slug, description, price, compare_at_price, category_id, active, is_featured, is_new, is_bestseller, brand, material, total_stock) VALUES
('Classic Oversized Tee', 'classic-oversized-tee', 'T-shirt oversize en coton premium, coupe droite et confortable.', 49.900, 65.000, 1, TRUE, TRUE, TRUE, FALSE, 'BY DJO', '100% Coton', 150),
('Streetwear Graphic Tee', 'streetwear-graphic-tee', 'T-shirt avec imprimé streetwear exclusif BY DJO.', 59.900, NULL, 1, TRUE, TRUE, TRUE, TRUE, 'BY DJO', '100% Coton', 100),
('Premium Slim Jeans', 'premium-slim-jeans', 'Jean slim en denim premium avec finition tendance.', 119.000, 149.000, 2, TRUE, TRUE, FALSE, TRUE, 'BY DJO', 'Denim', 80),
('Urban Cargo Pants', 'urban-cargo-pants', 'Pantalon cargo urbain avec poches latérales.', 99.000, NULL, 2, TRUE, TRUE, TRUE, FALSE, 'BY DJO', 'Coton', 60),
('Air Runner Sneakers', 'air-runner-sneakers', 'Sneakers confortables au design moderne et épuré.', 189.000, 239.000, 3, TRUE, TRUE, FALSE, TRUE, 'BY DJO', 'Synthétique', 40),
('Street Low Sneakers', 'street-low-sneakers', 'Sneakers basses lifestyle au style streetwear.', 159.000, NULL, 3, TRUE, FALSE, TRUE, FALSE, 'BY DJO', 'Cuir', 50),
('Luxury Snapback Cap', 'luxury-snapback-cap', 'Casquette snapback premium avec broderie BY DJO.', 39.000, NULL, 4, TRUE, TRUE, FALSE, FALSE, 'BY DJO', 'Coton', 200),
('Classic Dad Hat', 'classic-dad-hat', 'Casquette dad hat au style minimaliste.', 35.000, NULL, 4, TRUE, FALSE, TRUE, FALSE, 'BY DJO', 'Coton', 150),
('Bomber Jacket Premium', 'bomber-jacket-premium', 'Veste bomber en matériau premium avec doublure chaude.', 249.000, 329.000, 5, TRUE, TRUE, TRUE, TRUE, 'BY DJO', 'Polyester', 30),
('Varsity Jacket', 'varsity-jacket', 'Veste varsity classique revisité style streetwear.', 279.000, NULL, 5, TRUE, TRUE, FALSE, FALSE, 'BY DJO', 'Cuir/Mélange', 25),
('Summer Cargo Shorts', 'summer-cargo-shorts', 'Short cargo léger parfait pour l''été.', 79.000, 99.000, 6, TRUE, FALSE, TRUE, FALSE, 'BY DJO', 'Coton', 100),
('Premium Leather Belt', 'premium-leather-belt', 'Ceinture en cuir véritable avec boucle matte.', 59.000, NULL, 7, TRUE, TRUE, FALSE, FALSE, 'BY DJO', 'Cuir', 120),
('Minimal Watch', 'minimal-watch', 'Montre minimaliste avec bracelet en acier.', 189.000, 249.000, 7, TRUE, TRUE, FALSE, TRUE, 'BY DJO', 'Acier', 40),
('Streetwear Backpack', 'streetwear-backpack', 'Sac à dos streetwear avec compartiment laptop.', 129.000, NULL, 7, TRUE, FALSE, TRUE, FALSE, 'BY DJO', 'Polyester', 60);

-- Banners
INSERT INTO banners (title, subtitle, image_url, link, button_text, active, sort_order) VALUES
('Nouvelle Collection', 'Découvrez notre collection Premium 2024', '/uploads/banners/banner1.jpg', '/shop?category=new', 'Acheter Maintenant', TRUE, 1),
('Streetwear Drop', 'Les pièces les plus tendances du moment', '/uploads/banners/banner2.jpg', '/shop?category=t-shirts', 'Explorer', TRUE, 2),
('SOLDES -30%', 'Profitez des meilleures offres sur toute la collection', '/uploads/banners/banner3.jpg', '/shop?sale=true', 'Voir les Offres', TRUE, 3);

-- Sample Coupon
INSERT INTO coupons (code, description, discount_percentage, usage_limit, starts_at, expires_at, active) VALUES
('BYDJO10', '10% de réduction sur votre première commande', 10, 500, '2024-01-01', '2025-12-31', TRUE),
('WELCOME20', '20% de bienvenue', 20, 100, '2024-01-01', '2025-12-31', TRUE)
ON CONFLICT (code) DO NOTHING;
