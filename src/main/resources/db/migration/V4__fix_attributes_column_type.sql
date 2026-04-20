-- =====================================================
-- CHANGE FROM VARCHAR TO JSONB
-- =====================================================

ALTER TABLE products
    ALTER COLUMN attributes TYPE JSONB
        USING attributes::JSONB;

-- =====================================================
-- SET THE DEFAULT VALUE
-- =====================================================
ALTER TABLE products
    ALTER COLUMN attributes SET DEFAULT '{}'::JSONB;
