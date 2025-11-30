INSERT INTO users (username, password_hash, full_name, role)
	VALUES
		('admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'Admin User', 'ADMIN'),
		('anass','5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8','anass er','CASHIER'),
		('khalid','5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8','khalid hi','STOCK_MANAGER');

INSERT INTO products (name, description, price, quantity, min_stock, category)
VALUES
    ('Samsung Galaxy S24', 'Latest smartphone 256GB', 950.00, 8, 5, 'ELECTRONICS'),
    ('Wireless Mouse', 'Ergonomic optical mouse', 25.50, 50, 10, 'ELECTRONICS'),
    ('Clean Code', 'A Handbook of Agile Software Craftsmanship', 45.00, 30, 5, 'BOOKS'),
    ('The Pragmatic Programmer', 'Journey to Mastery', 55.00, 20, 5, 'BOOKS'),
    ('Men''s Cotton T-Shirt', 'Basic white t-shirt, size L', 15.00, 100, 20, 'FASHION'),
    ('Running Sneakers', 'Comfortable running shoes size 42', 89.99, 12, 4, 'FASHION'),
    ('Espresso Machine', 'Automatic coffee maker with grinder', 250.00, 10, 2, 'HOME_APPLIANCES'),
    ('Microwave Oven', '800W digital microwave', 120.00, 15, 3, 'HOME_APPLIANCES'),
    ('Arabica Coffee Beans', '1kg bag of premium beans', 18.50, 40, 10, 'GROCERIES'),
    ('Dark Chocolate', '70% Cocoa bar', 3.50, 200, 30, 'GROCERIES'),
    ('Yoga Mat', 'Non-slip exercise mat', 22.00, 25, 5, 'SPORTS_EQUIPMENT'),
    ('Dumbbell Set', '2x10kg adjustable weights', 55.00, 8, 2, 'SPORTS_EQUIPMENT');
