-- =====================================================================
-- STG seed data — 30 representative PENDING records for demo / testing
-- =====================================================================
INSERT INTO public.staging_synch_log
(id, table_name, table_reference, status, created_at, updated_at, retry_count, processed_at, node_id, notes, error_message)
VALUES
    (1, 'Laptop Pro 15', 'REF-001', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (2, 'Wireless Mouse', 'REF-002', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (3, 'USB-C Hub', 'REF-003', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (4, 'Laptop Pro 15', 'REF-004', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (5, 'Mechanical Keyboard', 'REF-005', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (6, '4K Monitor', 'REF-006', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (7, 'Wireless Mouse', 'REF-007', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (8, 'Webcam HD', 'REF-008', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (9, 'SSD 1TB', 'REF-009', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (10, 'USB-C Hub', 'REF-010', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (11, 'RAM 32GB', 'REF-011', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (12, '4K Monitor', 'REF-012', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (13, 'Laptop Pro 15', 'REF-013', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (14, 'Desk Stand', 'REF-014', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (15, 'SSD 1TB', 'REF-015', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (16, 'Mechanical Keyboard', 'REF-016', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (17, 'Webcam HD', 'REF-017', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (18, 'RAM 32GB', 'REF-018', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (19, 'Wireless Mouse', 'REF-019', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (20, 'Smart Speaker', 'REF-020', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (21, 'Laptop Pro 15', 'REF-021', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (22, '4K Monitor', 'REF-022', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (23, 'Desk Stand', 'REF-023', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (24, 'SSD 1TB', 'REF-024', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (25, 'Smart Speaker', 'REF-025', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (26, 'Wireless Mouse', 'REF-026', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (27, 'USB-C Hub', 'REF-027', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (28, 'Webcam HD', 'REF-028', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (29, 'RAM 32GB', 'REF-029', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (30, 'Mechanical Keyboard', 'REF-030', 'PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL);
