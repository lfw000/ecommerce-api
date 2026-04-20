-- =====================================================
-- REMOVE UNNECESSARY FIELDS ON USERS
-- =====================================================
ALTER TABLE users
    DROP COLUMN IF EXISTS created_by;
ALTER TABLE users
    DROP COLUMN IF EXISTS updated_by;

-- =====================================================
-- -- CREATE THE ADMIN ROLE
-- =====================================================
INSERT INTO roles (name, description, created_at, updated_at)
SELECT 'ROLE_ADMIN', 'Administrator with full system access', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_ADMIN');

-- =====================================================
-- -- INSERT A NEW ADMIN USER
-- =====================================================
INSERT INTO users (email, password, first_name, last_name, enabled, created_at, updated_at)
SELECT 'admin@example.com',
       '$2a$12$coO37ssUWeNx9YhwOgPFUendY5IjWcwqhtuhNRdSEKYUQSaeH6kVy', -- password: secureAdminPassword123!
       'Admin',
       'User',
       true,
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@example.com');

-- =====================================================
-- ASSIGN ADMIN ROLE TO THE ADMIN USER
-- =====================================================

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@example.com'
  AND r.name = 'ROLE_ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur
    WHERE ur.user_id = u.id
      AND ur.role_id = r.id);
