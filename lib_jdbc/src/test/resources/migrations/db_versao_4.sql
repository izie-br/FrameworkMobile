ALTER TABLE tb_customer RENAME TO tb_customer_tmp;
CREATE TABLE tb_customer (
    id integer NOT NULL PRIMARY KEY AUTOINCREMENT,
    name varchar(79),
    UNIQUE (name)
);
INSERT INTO tb_customer (id, name)
    SELECT id, name FROM tb_customer_tmp;
DROP TABLE tb_customer_tmp;
