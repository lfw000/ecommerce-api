ALTER TABLE orders ADD COLUMN IF NOT EXISTS cancelled_by BIGINT;
COMMENT ON COLUMN orders.cancelled_by IS 'User ID who cancelled the order';

ALTER TABLE orders ADD COLUMN IF NOT EXISTS cancelled_by_role VARCHAR(20);
COMMENT ON COLUMN orders.cancelled_by_role IS 'Role of user who cancelled (USER/ADMIN)';

ALTER TABLE orders ADD COLUMN IF NOT EXISTS cancellation_reason VARCHAR(50);
COMMENT ON COLUMN orders.cancellation_reason IS 'Reason for cancellation (USER_REQUESTED, OUT_OF_STOCK, etc.)';

ALTER TABLE orders ADD COLUMN IF NOT EXISTS cancellation_comment VARCHAR(500);
COMMENT ON COLUMN orders.cancellation_comment IS 'Additional comments about cancellation';

---

ALTER TABLE orders ADD COLUMN IF NOT EXISTS processed_at TIMESTAMP;
COMMENT ON COLUMN orders.processed_at IS 'When order moved to PROCESSING status';

ALTER TABLE orders ADD COLUMN IF NOT EXISTS refunded_at TIMESTAMP;
COMMENT ON COLUMN orders.refunded_at IS 'When order was refunded';

ALTER TABLE orders ADD COLUMN IF NOT EXISTS shipped_at TIMESTAMP;
COMMENT ON COLUMN orders.shipped_at IS 'When order was refunded';

ALTER TABLE orders ADD COLUMN IF NOT EXISTS payment_transaction_id VARCHAR(100);
COMMENT ON COLUMN orders.payment_transaction_id IS 'Transaction ID from payment gateway';

CREATE INDEX IF NOT EXISTS idx_orders_cancelled_by ON orders(cancelled_by);
CREATE INDEX IF NOT EXISTS idx_orders_cancellation_reason ON orders(cancellation_reason);

CREATE INDEX IF NOT EXISTS idx_orders_processed_at ON orders(processed_at);
CREATE INDEX IF NOT EXISTS idx_orders_refunded_at ON orders(refunded_at);
CREATE INDEX IF NOT EXISTS idx_orders_shipped_at ON orders(shipped_at);

---

SELECT status, COUNT(*) as count
FROM orders
GROUP BY status
ORDER BY status;

SELECT status, COUNT(*) as count
FROM payments
GROUP BY status
ORDER BY status;