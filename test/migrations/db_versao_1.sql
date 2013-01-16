CREATE TABLE tb_document (
    id integer NOT NULL PRIMARY KEY AUTOINCREMENT,
    title varchar(79) NOT NULL,
    created_at timestamp NOT NULL,
    id_author integer,
    text text NOT NULL
);
CREATE TRIGGER tb_document_on_insert AFTER INSERT ON tb_document BEGIN
  UPDATE tb_document SET id = new.id WHERE id = new.id;
END;
CREATE TABLE tb_author (
    id integer NOT NULL PRIMARY KEY AUTOINCREMENT,
    name varchar(79) NOT NULL,
    UNIQUE (name)
);
CREATE TABLE tb_score (
    id_author integer NOT NULL,
    id_document integer NOT NULL,
    score integer DEFAULT 0,
    PRIMARY KEY(id_author,id_document)
);
CREATE TABLE tb_customer (
    id integer NOT NULL PRIMARY KEY AUTOINCREMENT,
    name varchar(79) NOT NULL,
    UNIQUE (name)
);
CREATE TABLE tb_customer_join_document (
    id integer NOT NULL PRIMARY KEY AUTOINCREMENT,
    id_customer INTEGER NOT NULL,
    id_document INTEGER NOT NULL
);

