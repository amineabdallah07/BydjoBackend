-- BY DJO E-Commerce Database Schema
-- PostgreSQL

-- Roles
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

-- Users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100) UNIQUE,
    profile_image VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User Roles
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- OTP Codes
CREATE TABLE otp_codes (
    id BIGSERIAL PRIMARY KEY,
    phone VARCHAR(20) NOT NULL,
    code VARCHAR(6) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    attempts INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Categories
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100),
    description VARCHAR(500),
    image VARCHAR(255),
    parent_id BIGINT REFERENCES categories(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sizes
CREATE TABLE sizes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(10) NOT NULL UNIQUE
);

-- Colors
CREATE TABLE colors (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    hex_code VARCHAR(7) NOT NULL
);

-- Products
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(200),
    description TEXT,
    details TEXT,
    price DECIMAL(10,2) NOT NULL,
    compare_at_price DECIMAL(10,2),
    cost_price DECIMAL(10,2),
    category_id BIGINT NOT NULL REFERENCES categories(id),
    brand VARCHAR(50),
    sku VARCHAR(30),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    is_featured BOOLEAN DEFAULT FALSE,
    is_new BOOLEAN DEFAULT FALSE,
    is_bestseller BOOLEAN DEFAULT FALSE,
    is_flash_sale BOOLEAN DEFAULT FALSE,
    discount_percentage INTEGER DEFAULT 0,
    flash_sale_ends_at TIMESTAMP,
    material VARCHAR(50),
    tags VARCHAR(500),
    total_stock INTEGER DEFAULT 0,
    total_sold INTEGER DEFAULT 0,
    average_rating DOUBLE PRECISION DEFAULT 0,
    review_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Product Images
CREATE TABLE product_images (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    is_primary BOOLEAN DEFAULT FALSE,
    sort_order INTEGER DEFAULT 0,
    alt_text VARCHAR(100)
);

-- Product Variants
CREATE TABLE product_variants (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    size_id BIGINT REFERENCES sizes(id),
    color_id BIGINT REFERENCES colors(id),
    stock INTEGER DEFAULT 0,
    sku VARCHAR(50),
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT unique_variant UNIQUE (product_id, size_id, color_id)
);

-- Carts
CREATE TABLE carts (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(100),
    user_id BIGINT REFERENCES users(id),
    coupon_code VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Cart Items
CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    variant_id BIGINT REFERENCES product_variants(id),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL
);

-- Addresses
CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    label VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    governorate VARCHAR(50) NOT NULL,
    city VARCHAR(100) NOT NULL,
    address_line TEXT NOT NULL,
    notes VARCHAR(200),
    is_default BOOLEAN DEFAULT FALSE
);

-- Orders
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(30) NOT NULL UNIQUE,
    user_id BIGINT REFERENCES users(id),
    guest_name VARCHAR(100),
    guest_phone VARCHAR(20),
    guest_email VARCHAR(20),
    customer_phone VARCHAR(20) NOT NULL,
    shipping_full_name VARCHAR(100) NOT NULL,
    shipping_phone VARCHAR(20) NOT NULL,
    shipping_governorate VARCHAR(50) NOT NULL,
    shipping_city VARCHAR(100) NOT NULL,
    shipping_address TEXT NOT NULL,
    shipping_notes VARCHAR(200),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(20) NOT NULL DEFAULT 'CASH_ON_DELIVERY',
    subtotal DECIMAL(10,2) NOT NULL,
    discount DECIMAL(10,2) DEFAULT 0,
    shipping_cost DECIMAL(10,2) DEFAULT 0,
    total DECIMAL(10,2) NOT NULL,
    coupon_code VARCHAR(50),
    notes TEXT,
    confirmed_at TIMESTAMP,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Order Items
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    variant_id BIGINT REFERENCES product_variants(id),
    product_name VARCHAR(200) NOT NULL,
    size_name VARCHAR(50),
    color_name VARCHAR(50),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    product_image VARCHAR(500)
);

-- Wishlists
CREATE TABLE wishlists (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_wishlist UNIQUE (user_id, product_id)
);

-- Reviews
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id),
    guest_name VARCHAR(100),
    rating INTEGER DEFAULT 5,
    title VARCHAR(500),
    comment TEXT,
    approved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Coupons
CREATE TABLE coupons (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200) NOT NULL,
    discount_percentage INTEGER DEFAULT 0,
    discount_amount DECIMAL(10,2),
    min_order_amount DECIMAL(10,2),
    max_discount_amount DECIMAL(10,2),
    usage_limit INTEGER DEFAULT 100,
    used_count INTEGER DEFAULT 0,
    starts_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Notifications
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    message TEXT,
    type VARCHAR(50),
    link VARCHAR(500),
    read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Banners
CREATE TABLE banners (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    subtitle VARCHAR(500),
    image_url VARCHAR(500) NOT NULL,
    link VARCHAR(200),
    button_text VARCHAR(50),
    active BOOLEAN DEFAULT TRUE,
    sort_order INTEGER DEFAULT 0,
    starts_at TIMESTAMP,
    ends_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_active ON products(active);
CREATE INDEX idx_products_slug ON products(slug);
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_number ON orders(order_number);
CREATE INDEX idx_carts_user ON carts(user_id);
CREATE INDEX idx_carts_session ON carts(session_id);
CREATE INDEX idx_otp_phone ON otp_codes(phone);
CREATE INDEX idx_wishlist_user ON wishlists(user_id);
