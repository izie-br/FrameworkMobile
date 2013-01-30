CREATE TABLE tb_documents_document(
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  created_at TIMESTAMP NOT NULL,
  id_author INTEGER,
  text TEXT NOT NULL,
  title TEXT NOT NULL
);
DROP TABLE tb_document;
