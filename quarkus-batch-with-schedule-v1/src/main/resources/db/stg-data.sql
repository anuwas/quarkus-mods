-- =====================================================
-- STG Database - Seed Data for Demo Batch Processing
-- =====================================================

-- Insert sample sales transactions
INSERT INTO stg_sales_transaction (id, transaction_date, product_code, product_name, category, quantity, unit_price, customer_id, region, status, created_at)
VALUES
  (1,  '2024-01-15', 'PROD-001', 'Laptop Pro 15',      'Electronics', 2,  1299.99, 'CUST-101', 'NORTH', 'PENDING', NOW()),
  (2,  '2024-01-15', 'PROD-002', 'Wireless Mouse',      'Accessories', 5,    29.99, 'CUST-102', 'SOUTH', 'PENDING', NOW()),
  (3,  '2024-01-15', 'PROD-003', 'USB-C Hub',           'Accessories', 3,    49.99, 'CUST-103', 'EAST',  'PENDING', NOW()),
  (4,  '2024-01-16', 'PROD-001', 'Laptop Pro 15',       'Electronics', 1,  1299.99, 'CUST-104', 'WEST',  'PENDING', NOW()),
  (5,  '2024-01-16', 'PROD-004', 'Mechanical Keyboard', 'Accessories', 4,    89.99, 'CUST-105', 'NORTH', 'PENDING', NOW()),
  (6,  '2024-01-16', 'PROD-005', '4K Monitor',          'Electronics', 2,   599.99, 'CUST-101', 'NORTH', 'PENDING', NOW()),
  (7,  '2024-01-17', 'PROD-002', 'Wireless Mouse',      'Accessories', 10,   29.99, 'CUST-106', 'SOUTH', 'PENDING', NOW()),
  (8,  '2024-01-17', 'PROD-006', 'Webcam HD',           'Electronics', 3,   129.99, 'CUST-107', 'EAST',  'PENDING', NOW()),
  (9,  '2024-01-17', 'PROD-007', 'SSD 1TB',             'Storage',     5,   119.99, 'CUST-108', 'WEST',  'PENDING', NOW()),
  (10, '2024-01-18', 'PROD-003', 'USB-C Hub',           'Accessories', 7,    49.99, 'CUST-109', 'NORTH', 'PENDING', NOW()),
  (11, '2024-01-18', 'PROD-008', 'RAM 32GB',            'Storage',     4,    89.99, 'CUST-110', 'SOUTH', 'PENDING', NOW()),
  (12, '2024-01-18', 'PROD-005', '4K Monitor',          'Electronics', 1,   599.99, 'CUST-111', 'EAST',  'PENDING', NOW()),
  (13, '2024-01-19', 'PROD-001', 'Laptop Pro 15',       'Electronics', 3,  1299.99, 'CUST-112', 'WEST',  'PENDING', NOW()),
  (14, '2024-01-19', 'PROD-009', 'Desk Stand',          'Accessories', 6,    39.99, 'CUST-113', 'NORTH', 'PENDING', NOW()),
  (15, '2024-01-19', 'PROD-007', 'SSD 1TB',             'Storage',     2,   119.99, 'CUST-114', 'SOUTH', 'PENDING', NOW()),
  (16, '2024-01-20', 'PROD-004', 'Mechanical Keyboard', 'Accessories', 3,    89.99, 'CUST-115', 'EAST',  'PENDING', NOW()),
  (17, '2024-01-20', 'PROD-006', 'Webcam HD',           'Electronics', 5,   129.99, 'CUST-116', 'WEST',  'PENDING', NOW()),
  (18, '2024-01-20', 'PROD-008', 'RAM 32GB',            'Storage',     8,    89.99, 'CUST-117', 'NORTH', 'PENDING', NOW()),
  (19, '2024-01-21', 'PROD-002', 'Wireless Mouse',      'Accessories', 15,   29.99, 'CUST-118', 'SOUTH', 'PENDING', NOW()),
  (20, '2024-01-21', 'PROD-010', 'Smart Speaker',       'Electronics', 4,   199.99, 'CUST-119', 'EAST',  'PENDING', NOW()),
  (21, '2024-01-21', 'PROD-001', 'Laptop Pro 15',       'Electronics', 2,  1299.99, 'CUST-120', 'WEST',  'PENDING', NOW()),
  (22, '2024-01-22', 'PROD-005', '4K Monitor',          'Electronics', 3,   599.99, 'CUST-101', 'NORTH', 'PENDING', NOW()),
  (23, '2024-01-22', 'PROD-009', 'Desk Stand',          'Accessories', 10,   39.99, 'CUST-102', 'SOUTH', 'PENDING', NOW()),
  (24, '2024-01-22', 'PROD-007', 'SSD 1TB',             'Storage',     6,   119.99, 'CUST-103', 'EAST',  'PENDING', NOW()),
  (25, '2024-01-23', 'PROD-010', 'Smart Speaker',       'Electronics', 2,   199.99, 'CUST-104', 'WEST',  'PENDING', NOW());
INSERT INTO stg_sales_transaction (id, transaction_date, product_code, product_name, category, quantity, unit_price, customer_id, region, status, created_at)
VALUES
    (26,  '2024-01-15', 'PROD-001', 'Laptop Pro 15',      'Electronics', 4,  1299.99, 'CUST-101', 'NORTH', 'PENDING', NOW()),
    (27,  '2024-01-15', 'PROD-002', 'Wireless Mouse',      'Accessories', 10,    29.99, 'CUST-102', 'SOUTH', 'PENDING', NOW()),
    (28,  '2024-01-15', 'PROD-003', 'USB-C Hub',           'Accessories', 6,    49.99, 'CUST-103', 'EAST',  'PENDING', NOW()),
    (29,  '2024-01-16', 'PROD-001', 'Laptop Pro 15',       'Electronics', 2,  1299.99, 'CUST-104', 'WEST',  'PENDING', NOW()),
    (30,  '2024-01-16', 'PROD-004', 'Mechanical Keyboard', 'Accessories', 8,    89.99, 'CUST-105', 'NORTH', 'PENDING', NOW()),
    (31,  '2024-01-16', 'PROD-005', '4K Monitor',          'Electronics', 4,   599.99, 'CUST-101', 'NORTH', 'PENDING', NOW()),
    (32,  '2024-01-17', 'PROD-002', 'Wireless Mouse',      'Accessories', 20,   29.99, 'CUST-106', 'SOUTH', 'PENDING', NOW()),
    (33,  '2024-01-17', 'PROD-006', 'Webcam HD',           'Electronics', 6,   129.99, 'CUST-107', 'EAST',  'PENDING', NOW()),
    (34,  '2024-01-17', 'PROD-007', 'SSD 1TB',             'Storage',     10,   119.99, 'CUST-108', 'WEST',  'PENDING', NOW()),
    (35, '2024-01-18', 'PROD-003', 'USB-C Hub',           'Accessories', 14,    49.99, 'CUST-109', 'NORTH', 'PENDING', NOW()),
    (36, '2024-01-18', 'PROD-008', 'RAM 32GB',            'Storage',     8,    89.99, 'CUST-110', 'SOUTH', 'PENDING', NOW()),
    (37, '2024-01-18', 'PROD-005', '4K Monitor',          'Electronics', 2,   599.99, 'CUST-111', 'EAST',  'PENDING', NOW()),
    (38, '2024-01-19', 'PROD-001', 'Laptop Pro 15',       'Electronics', 6,  1299.99, 'CUST-112', 'WEST',  'PENDING', NOW()),
    (39, '2024-01-19', 'PROD-009', 'Desk Stand',          'Accessories', 12,    39.99, 'CUST-113', 'NORTH', 'PENDING', NOW()),
    (40, '2024-01-19', 'PROD-007', 'SSD 1TB',             'Storage',     4,   119.99, 'CUST-114', 'SOUTH', 'PENDING', NOW()),
    (41, '2024-01-20', 'PROD-004', 'Mechanical Keyboard', 'Accessories', 6,    89.99, 'CUST-115', 'EAST',  'PENDING', NOW()),
    (42, '2024-01-20', 'PROD-006', 'Webcam HD',           'Electronics', 10,   129.99, 'CUST-116', 'WEST',  'PENDING', NOW()),
    (43, '2024-01-20', 'PROD-008', 'RAM 32GB',            'Storage',     16,    89.99, 'CUST-117', 'NORTH', 'PENDING', NOW()),
    (44, '2024-01-21', 'PROD-002', 'Wireless Mouse',      'Accessories', 30,   29.99, 'CUST-118', 'SOUTH', 'PENDING', NOW()),
    (45, '2024-01-21', 'PROD-010', 'Smart Speaker',       'Electronics', 8,   199.99, 'CUST-119', 'EAST',  'PENDING', NOW()),
    (46, '2024-01-21', 'PROD-001', 'Laptop Pro 15',       'Electronics', 4,  1299.99, 'CUST-120', 'WEST',  'PENDING', NOW()),
    (47, '2024-01-22', 'PROD-005', '4K Monitor',          'Electronics', 6,   599.99, 'CUST-101', 'NORTH', 'PENDING', NOW()),
    (48, '2024-01-22', 'PROD-009', 'Desk Stand',          'Accessories', 20,   39.99, 'CUST-102', 'SOUTH', 'PENDING', NOW()),
    (49, '2024-01-22', 'PROD-007', 'SSD 1TB',             'Storage',     12,   119.99, 'CUST-103', 'EAST',  'PENDING', NOW()),
    (50, '2024-01-23', 'PROD-010', 'Smart Speaker',       'Electronics', 4,   199.99, 'CUST-104', 'WEST',  'PENDING', NOW());


