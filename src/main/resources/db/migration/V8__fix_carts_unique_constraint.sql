ALTER TABLE carts DROP CONSTRAINT IF EXISTS carts_user_id_key;

CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_active_cart_per_user
ON carts (user_id) WHERE active = true;

WITH duplicates AS (
    SELECT id,
           user_id,
           created_at,
           ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY created_at DESC) AS rn
    FROM carts
    WHERE active = true
)
UPDATE carts
SET active = false
FROM duplicates
WHERE carts.id = duplicates.id AND duplicates.rn > 1;