-- Reassign products from unwanted categories to T-Shirts (id=1)
UPDATE products SET category_id = 1 WHERE category_id IN (2, 3, 4, 5, 7);

-- Delete unused categories
DELETE FROM categories WHERE id IN (2, 3, 4, 5, 7);
