ALTER TABLE payments
ALTER COLUMN payment_details TYPE TEXT USING payment_details::TEXT;