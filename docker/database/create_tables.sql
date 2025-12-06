
-- ENUM TYPES

CREATE TYPE user_role AS ENUM ('ADMIN','STOCK_MANAGER','CASHIER');
CREATE TYPE category AS ENUM ('ELECTRONICS','FASHION','HOME_APPLIANCES','BOOKS','TOYS','GROCERIES','BEAUTY_PRODUCTS','SPORTS_EQUIPMENT');

-- USERS

CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(120),
    role user_role NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);


-- PRODUCTS

CREATE TABLE products (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    quantity INTEGER NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    min_stock INTEGER NOT NULL DEFAULT 0 CHECK (min_stock >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
	category category NOT NULL
);


-- SALES

CREATE TABLE sales (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	total_price NUMERIC(12,2) NOT NULL CHECK (total_price >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);


-- SALE ITEMS

CREATE TABLE sale_items (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sale_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(10,2) NOT NULL CHECK (unit_price >= 0),
    line_total NUMERIC(12,2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
);

-- MATERIALIZED VIEWS

CREATE MATERIALIZED VIEW mv_product_stats AS
SELECT
    COUNT(*) as total_products,
    COUNT(*) FILTER (WHERE quantity <= min_stock) as low_stock,
    COUNT(*) FILTER (WHERE quantity > 0) as in_stock,
    COUNT(*) FILTER (WHERE quantity = 0) as out_of_stock
FROM products;

CREATE MATERIALIZED VIEW mv_sales_stats AS
SELECT
    DATE(s.created_at) as sale_date,
    COUNT(s.id) as total_sales_count,
    COALESCE(SUM(s.total_price), 0) as total_revenue,
    COALESCE(SUM(si.quantity), 0) as total_items_sold
FROM sales s
LEFT JOIN sale_items si ON s.id = si.sale_id
GROUP BY DATE(s.created_at);

-- INDEXES

CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_sales_created_at ON sales(created_at);
CREATE INDEX idx_sale_items_product_id ON sale_items(product_id);
CREATE INDEX idx_mv_sales_date ON mv_sales_stats(sale_date);
