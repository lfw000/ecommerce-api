-- =====================================================
-- PostgreSQL 15+
-- =====================================================

-- Create the database
-- CREATE DATABASE ecommerce_db;
-- \c ecommerce_db;

-- =====================================================
-- TABLE: roles
-- =====================================================
CREATE TABLE roles
(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE,
    description VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- =====================================================
-- TABLE: users
-- =====================================================
CREATE TABLE users
(
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(60) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- =====================================================
-- TABLE: user_roles
-- =====================================================
CREATE TABLE user_roles
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT
);

-- =====================================================
-- TABLE: addresses
-- =====================================================
CREATE TABLE addresses
(
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    street VARCHAR(100) NOT NULL,
    address_line2 VARCHAR(100),
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    zip_code VARCHAR(10) NOT NULL,
    country VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    address_type VARCHAR(20) NOT NULL DEFAULT 'SHIPPING',
    delivery_instructions VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_addresses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- Índice único para garantizar una sola dirección default por usuario
CREATE UNIQUE INDEX idx_unique_default_address ON addresses (user_id) WHERE is_default = true;

-- =====================================================
-- TABLE: categories
-- =====================================================
CREATE TABLE categories
(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    parent_category_id BIGINT,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_category_id) REFERENCES categories(id) ON DELETE RESTRICT
);

-- =====================================================
-- TABLE: products
-- =====================================================
CREATE TABLE products
(
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    category_id BIGINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    attributes JSON,
    weight DECIMAL(10,2),
    length DECIMAL(10,2),
    width DECIMAL(10,2),
    height DECIMAL(10,2),
    low_stock_threshold INTEGER DEFAULT 5,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
);

-- =====================================================
-- TABLE: product_images
-- =====================================================
CREATE TABLE product_images
(
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(255),
    display_order INTEGER NOT NULL DEFAULT 0,
    is_main BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- =====================================================
-- TABLE: carts
-- =====================================================
CREATE TABLE carts
(
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    expires_at TIMESTAMP,
    converted_to_order BOOLEAN NOT NULL DEFAULT FALSE,
    converted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- =====================================================
-- TABLE: cart_items
-- =====================================================
CREATE TABLE cart_items
(
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    CONSTRAINT chk_cart_items_quantity CHECK (quantity > 0),
    CONSTRAINT chk_cart_items_price CHECK (price > 0)
);

-- =====================================================
-- TABLE: orders
-- =====================================================
CREATE TABLE orders
(
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(36) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    subtotal DECIMAL(10,2) NOT NULL,
    shipping_cost DECIMAL(10,2) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(10,2) NOT NULL,
    shipping_address_id BIGINT NOT NULL,
    billing_address_id BIGINT NOT NULL,
    shipping_method VARCHAR(50),
    tracking_number VARCHAR(100),
    estimated_delivery_date TIMESTAMP,
    delivered_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancellation_reason VARCHAR(500),
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_orders_shipping_address FOREIGN KEY (shipping_address_id) REFERENCES addresses(id) ON DELETE RESTRICT,
    CONSTRAINT fk_orders_billing_address FOREIGN KEY (billing_address_id) REFERENCES addresses(id) ON DELETE RESTRICT
);

-- =====================================================
-- TABLE: order_items
-- =====================================================
CREATE TABLE order_items
(
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    product_snapshot JSON,
    discount_percentage INTEGER DEFAULT 0,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    final_price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    CONSTRAINT chk_order_items_quantity CHECK (quantity > 0),
    CONSTRAINT chk_order_items_price CHECK (unit_price > 0)
);

-- =====================================================
-- TABLE: payments
-- =====================================================
CREATE TABLE payments
(
    id BIGSERIAL PRIMARY KEY,
    payment_number VARCHAR(36) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL UNIQUE,
    payment_method VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    transaction_id VARCHAR(100),
    payment_details JSON,
    paid_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason VARCHAR(500),
    refunded_at TIMESTAMP,
    refund_amount DECIMAL(10,2),
    refund_reason VARCHAR(500),
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE RESTRICT
);

-- =====================================================
-- TABLE: refund_transactions
-- =====================================================
CREATE TABLE refund_transactions
(
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    refund_number VARCHAR(36) NOT NULL UNIQUE,
    amount DECIMAL(10,2) NOT NULL,
    refund_type VARCHAR(20) NOT NULL,
    reason VARCHAR(500),
    refunded_at TIMESTAMP NOT NULL,
    transaction_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_refund_payment FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE
);

-- =====================================================
-- ÍNDICES ADICIONALES PARA RENDIMIENTO
-- =====================================================

-- Índices para búsquedas frecuentes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_enabled ON users(enabled);
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_active ON products(active);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_order_number ON orders(order_number);
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);
CREATE INDEX idx_carts_user_active ON carts(user_id, active);
CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);
CREATE INDEX idx_payments_order ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_transaction ON payments(transaction_id);

-- =====================================================
-- INITIAL DATA (SEED)
-- =====================================================

-- Insert base roles
INSERT INTO roles (name, description, created_at, updated_at)
VALUES
('ROLE_USER', 'Standard user with shopping permissions', NOW(), NOW()),
('ROLE_ADMIN', 'Administrator with full system access', NOW(), NOW());

-- Insert base categories
INSERT INTO categories (name, description, active, display_order, created_at, updated_at)
VALUES
('Electrónicos', 'Productos electrónicos y gadgets', true, 1, NOW(), NOW()),
('Ropa', 'Prendas de vestir para hombre, mujer y niños', true, 2, NOW(), NOW()),
('Hogar', 'Artículos para el hogar y decoración', true, 3, NOW(), NOW()),
('Deportes', 'Equipamiento y accesorios deportivos', true, 4, NOW(), NOW()),
('Libros', 'Libros, eBooks y material educativo', true, 5, NOW(), NOW());

-- =====================================================
-- DATABASE PERMISSIONS AND ROLES (OPTIONAL)
-- =====================================================

-- Create an application user (for production)
-- CREATE USER ecommerce_app WITH PASSWORD 'secure_password';
-- GRANT CONNECT ON DATABASE ecommerce_db TO ecommerce_app;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO ecommerce_app;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO ecommerce_app;