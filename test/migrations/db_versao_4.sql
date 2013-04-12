ALTER TABLE tb_score RENAME TO tb_score_tmp;
CREATE TABLE tb_score (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    id_author integer NOT NULL,
    id_document integer NOT NULL,
    active integer,
    score integer DEFAULT 0
);
INSERT INTO tb_score (id_author, id_document, score)
    SELECT id_author, id_document, score FROM tb_score_tmp
    WHERE id_document NOTNULL;
DROP TABLE tb_score_tmp;

