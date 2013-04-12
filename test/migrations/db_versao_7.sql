ALTER TABLE tb_customer RENAME TO tb_customer_temp;
CREATE TABLE tb_customer(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT);
INSERT INTO tb_customer(id, name)
    SELECT id, name FROM tb_customer_temp;
DROP TABLE tb_customer_temp;
