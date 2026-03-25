-- =====================================================================
-- STG seed data — 30 representative PENDING records for demo / testing
-- =====================================================================
INSERT INTO public.staging_synch_log
(id, table_name, quantity, status, created_at, updated_at, retry_count, processed_at, node_id, notes, error_message)
VALUES
    (1, 'Laptop Pro 15', 2, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (2, 'Wireless Mouse', 5, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (3, 'USB-C Hub', 3, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (4, 'Laptop Pro 15', 1, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (5, 'Mechanical Keyboard', 4, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (6, '4K Monitor', 2, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (7, 'Wireless Mouse',10, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (8, 'Webcam HD', 3, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (9, 'SSD 1TB', 5, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (10, 'USB-C Hub', 7, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (11, 'RAM 32GB', 4, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (12, '4K Monitor', 1, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (13, 'Laptop Pro 15', 3, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (14, 'Desk Stand', 6, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (15, 'SSD 1TB', 2, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (16, 'Mechanical Keyboard', 3, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (17, 'Webcam HD', 5, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (18, 'RAM 32GB', 8, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (19, 'Wireless Mouse',15, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (20, 'Smart Speaker', 4, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (21, 'Laptop Pro 15', 2, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (22, '4K Monitor', 3, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (23, 'Desk Stand',10, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (24, 'SSD 1TB', 6, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (25, 'Smart Speaker', 2, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (26, 'Wireless Mouse', 8, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (27, 'USB-C Hub', 4, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (28, 'Webcam HD', 6, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (29, 'RAM 32GB', 3, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (30, 'Mechanical Keyboard', 5, 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL);
