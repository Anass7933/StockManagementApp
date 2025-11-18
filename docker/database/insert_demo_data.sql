INSERT INTO users (username, password_hash, full_name, role)
	VALUES
		('admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'Admin User', 'ADMIN');

INSERT INTO products (name, description, price, quantity, min_stock, category)
	VALUES
		('Laptop', '15 inch gaming laptop', 1200.00, 10, 2,'tech'),
		('Mouse', 'Wireless mouse', 25.00, 50, 5,'tech'),
		('Keyboard', 'Mechanical keyboard', 75.00, 20, 3,'tech'),
		('Monitor', '24 inch LED monitor', 180.00, 15, 2,'tech');
