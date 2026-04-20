-- =====================================================
-- ELECTRONICS (category_id = 1)
-- =====================================================

INSERT INTO products (sku, name, description, price, stock, category_id, active, featured, created_at, updated_at) VALUES
('ELEC-001', 'Laptop Gamer Pro', 'Laptop de alta gama con procesador Intel i9, 32GB RAM, 1TB SSD', 1599.99, 15, 1, true, true, NOW(), NOW()),
('ELEC-002', 'Mouse Inalámbrico', 'Mouse ergonómico recargable con 6 botones programables', 45.99, 50, 1, true, false, NOW(), NOW()),
('ELEC-003', 'Teclado Mecánico', 'Teclado mecánico RGB con switches rojos', 89.99, 30, 1, true, true, NOW(), NOW()),
('ELEC-004', 'Monitor 27" 4K', 'Monitor Ultra HD 4K, 144Hz, 1ms respuesta', 499.99, 10, 1, true, false, NOW(), NOW()),
('ELEC-005', 'Auriculares Bluetooth', 'Auriculares inalámbricos con cancelación de ruido', 79.99, 25, 1, true, false, NOW(), NOW());

-- =====================================================
-- CLOTHING_PRODUCTS (category_id = 2)
-- =====================================================

INSERT INTO products (sku, name, description, price, stock, category_id, active, featured, created_at, updated_at) VALUES
('ROPA-001', 'Camiseta Algodón', 'Camiseta 100% algodón, varios colores', 19.99, 100, 2, true, false, NOW(), NOW()),
('ROPA-002', 'Jeans Clásicos', 'Jeans corte recto, azul denim', 49.99, 60, 2, true, true, NOW(), NOW()),
('ROPA-003', 'Chaqueta Cuero', 'Chaqueta de cuero genuino, estilo motoquero', 129.99, 20, 2, true, false, NOW(), NOW()),
('ROPA-004', 'Zapatillas Deportivas', 'Zapatillas running, tallas 36-44', 79.99, 45, 2, true, true, NOW(), NOW()),
('ROPA-005', 'Gorra Deportiva', 'Gorra ajustable, protección UV', 15.99, 80, 2, true, false, NOW(), NOW());

-- =====================================================
-- HOUSEHOLD PRODUCTS (category_id = 3)
-- =====================================================

INSERT INTO products (sku, name, description, price, stock, category_id, active, featured, created_at, updated_at) VALUES
('HOGAR-001', 'Juego de Sábanas', 'Juego de sábanas 100% algodón, 2 fundas', 39.99, 40, 3, true, false, NOW(), NOW()),
('HOGAR-002', 'Lámpara LED', 'Lámpara de escritorio LED regulable', 29.99, 35, 3, true, true, NOW(), NOW()),
('HOGAR-003', 'Set de Ollas', 'Set de 5 ollas antiadherentes', 89.99, 15, 3, true, false, NOW(), NOW()),
('HOGAR-004', 'Cojín Decorativo', 'Cojín 45x45cm, varios diseños', 12.99, 70, 3, true, false, NOW(), NOW()),
('HOGAR-005', 'Organizador de Escritorio', 'Organizador con 3 compartimentos', 18.99, 55, 3, true, false, NOW(), NOW());

-- =====================================================
-- SPORTS PRODUCTS (category_id = 4)
-- =====================================================

INSERT INTO products (sku, name, description, price, stock, category_id, active, featured, created_at, updated_at) VALUES
('DEP-001', 'Pelota de Fútbol', 'Pelota talla 5, cosida a máquina', 24.99, 30, 4, true, true, NOW(), NOW()),
('DEP-002', 'Raqueta de Tenis', 'Raqueta de carbono, peso balanceado', 59.99, 20, 4, true, false, NOW(), NOW()),
('DEP-003', 'Yoga Mat', 'Esterilla antideslizante 10mm', 29.99, 45, 4, true, false, NOW(), NOW()),
('DEP-004', 'Banda Elástica', 'Set de 5 bandas de resistencia', 14.99, 90, 4, true, false, NOW(), NOW()),
('DEP-005', 'Botella Deportiva', 'Botella acero inoxidable 1L', 17.99, 65, 4, true, true, NOW(), NOW());

-- =====================================================
-- BOOKS (category_id = 5)
-- =====================================================

INSERT INTO products (sku, name, description, price, stock, category_id, active, featured, created_at, updated_at) VALUES
('LIBRO-001', 'Clean Code', 'Libro de buenas prácticas de programación', 45.99, 25, 5, true, true, NOW(), NOW()),
('LIBRO-002', 'El Principito', 'Clásico de la literatura universal', 12.99, 50, 5, true, false, NOW(), NOW()),
('LIBRO-003', 'Spring Boot en Acción', 'Guía práctica de Spring Boot', 59.99, 15, 5, true, true, NOW(), NOW()),
('LIBRO-004', 'Cien Años de Soledad', 'Novela de Gabriel García Márquez', 18.99, 40, 5, true, false, NOW(), NOW()),
('LIBRO-005', 'Design Patterns', 'Patrones de diseño GoF', 49.99, 20, 5, true, false, NOW(), NOW());

-- =====================================================
-- CHECK INSERTIONS
-- =====================================================

DO $$
    DECLARE
        product_count INTEGER;
    BEGIN
        SELECT COUNT(*) INTO product_count FROM products;
        RAISE NOTICE 'Total de productos insertados: %', product_count;
    END $$;