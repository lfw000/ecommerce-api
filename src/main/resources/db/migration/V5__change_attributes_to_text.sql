ALTER TABLE products
    ALTER COLUMN attributes TYPE TEXT USING attributes::TEXT;
