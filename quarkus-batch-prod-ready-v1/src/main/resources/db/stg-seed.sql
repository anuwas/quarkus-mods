-- =====================================================================
-- STG seed data — 30 representative PENDING records for demo / testing
-- =====================================================================
INSERT INTO public.staging_record
(id, product_code, product_name, category, region, customer_id, quantity, unit_price, transaction_date, status, created_at, updated_at, retry_count, processed_at, node_id, notes, error_message)
VALUES
    (1, 'PROD-001','Laptop Pro 15','Electronics','NORTH','CUST-101', 2, 1299.9900,'2024-01-15','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (2, 'PROD-002','Wireless Mouse','Accessories','SOUTH','CUST-102', 5,   29.9900,'2024-01-15','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (3, 'PROD-003','USB-C Hub','Accessories','EAST','CUST-103', 3,   49.9900,'2024-01-15','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (4, 'PROD-001','Laptop Pro 15','Electronics','WEST','CUST-104', 1, 1299.9900,'2024-01-16','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (5, 'PROD-004','Mechanical Keyboard','Accessories','NORTH','CUST-105', 4,   89.9900,'2024-01-16','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (6, 'PROD-005','4K Monitor','Electronics','NORTH','CUST-101', 2,  599.9900,'2024-01-16','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (7, 'PROD-002','Wireless Mouse','Accessories','SOUTH','CUST-106',10,   29.9900,'2024-01-17','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (8, 'PROD-006','Webcam HD','Electronics','EAST','CUST-107', 3,  129.9900,'2024-01-17','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (9, 'PROD-007','SSD 1TB','Storage','WEST','CUST-108', 5,  119.9900,'2024-01-17','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (10, 'PROD-003','USB-C Hub','Accessories','NORTH','CUST-109', 7,   49.9900,'2024-01-18','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (11, 'PROD-008','RAM 32GB','Storage','SOUTH','CUST-110', 4,   89.9900,'2024-01-18','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (12, 'PROD-005','4K Monitor','Electronics','EAST','CUST-111', 1,  599.9900,'2024-01-18','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (13, 'PROD-001','Laptop Pro 15','Electronics','WEST','CUST-112', 3, 1299.9900,'2024-01-19','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (14, 'PROD-009','Desk Stand','Accessories','NORTH','CUST-113', 6,   39.9900,'2024-01-19','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (15, 'PROD-007','SSD 1TB','Storage','SOUTH','CUST-114', 2,  119.9900,'2024-01-19','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (16, 'PROD-004','Mechanical Keyboard','Accessories','EAST','CUST-115', 3,   89.9900,'2024-01-20','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (17, 'PROD-006','Webcam HD','Electronics','WEST','CUST-116', 5,  129.9900,'2024-01-20','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (18, 'PROD-008','RAM 32GB','Storage','NORTH','CUST-117', 8,   89.9900,'2024-01-20','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (19, 'PROD-002','Wireless Mouse','Accessories','SOUTH','CUST-118',15,   29.9900,'2024-01-21','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (20, 'PROD-010','Smart Speaker','Electronics','EAST','CUST-119', 4,  199.9900,'2024-01-21','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (21, 'PROD-001','Laptop Pro 15','Electronics','WEST','CUST-120', 2, 1299.9900,'2024-01-21','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (22, 'PROD-005','4K Monitor','Electronics','NORTH','CUST-101', 3,  599.9900,'2024-01-22','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (23, 'PROD-009','Desk Stand','Accessories','SOUTH','CUST-102',10,   39.9900,'2024-01-22','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (24, 'PROD-007','SSD 1TB','Storage','EAST','CUST-103', 6,  119.9900,'2024-01-22','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (25, 'PROD-010','Smart Speaker','Electronics','WEST','CUST-104', 2,  199.9900,'2024-01-23','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (26, 'PROD-002','Wireless Mouse','Accessories','NORTH','CUST-105', 8,   29.9900,'2024-01-23','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (27, 'PROD-003','USB-C Hub','Accessories','SOUTH','CUST-106', 4,   49.9900,'2024-01-23','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (28, 'PROD-006','Webcam HD','Electronics','EAST','CUST-107', 6,  129.9900,'2024-01-24','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (29, 'PROD-008','RAM 32GB','Storage','WEST','CUST-108', 3,   89.9900,'2024-01-24','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL),
    (30, 'PROD-004','Mechanical Keyboard','Accessories','NORTH','CUST-109', 5,   89.9900,'2024-01-24','PENDING',NOW(),NOW(),0,NULL,NULL,NULL,NULL);
