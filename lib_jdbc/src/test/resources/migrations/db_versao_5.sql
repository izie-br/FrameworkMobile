CREATE TABLE fmvv_sync(id INTEGER PRIMARY KEY AUTOINCREMENT, id_server INTEGER, classname TEXT NOT NULL);
INSERT INTO fmvv_sync (id, classname) VALUES (1, 'offsetval');
CREATE TABLE fmvv_to_sync(id INTEGER PRIMARY KEY, id_user INTEGER NOT NULL, classname TEXT NOT NULL, action INTEGER NOT NULL);