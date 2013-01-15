DROP TABLE tb_author;
CREATE TABLE tb_author (
    id integer NOT NULL PRIMARY KEY AUTOINCREMENT,
    name varchar(79) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    active BOOLEAN NOT NULL,
    UNIQUE (name)
);

