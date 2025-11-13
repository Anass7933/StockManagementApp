INSERT INTO users (username, password_hash, full_name, role)
	VALUES
		('admin', '$2a$12$EXAMPLERANDOMHASHSTRING1234567890', 'Admin User', 'ADMIN');

INSERT INTO products (supplier_id, name, description, price, quantity, min_stock)
	VALUES
		(1, 'Laptop', '15 inch gaming laptop', 1200.00, 10, 2),
		(1, 'Mouse', 'Wireless mouse', 25.00, 50, 5),
		(2, 'Keyboard', 'Mechanical keyboard', 75.00, 20, 3),
		(2, 'Monitor', '24 inch LED monitor', 180.00, 15, 2);
