PRAGMA foreign_keys=OFF;
BEGIN TRANSACTION;
CREATE TABLE "auth_permission" (
    "id" integer NOT NULL PRIMARY KEY,
    "name" varchar(50) NOT NULL,
    "content_type_id" integer NOT NULL,
    "codename" varchar(100) NOT NULL,
    UNIQUE ("content_type_id", "codename")
);
INSERT INTO "auth_permission" VALUES(1,'Can add group',2,'add_group');
INSERT INTO "auth_permission" VALUES(2,'Can add permission',1,'add_permission');
INSERT INTO "auth_permission" VALUES(3,'Can add user',3,'add_user');
INSERT INTO "auth_permission" VALUES(4,'Can change group',2,'change_group');
INSERT INTO "auth_permission" VALUES(5,'Can change permission',1,'change_permission');
INSERT INTO "auth_permission" VALUES(6,'Can change user',3,'change_user');
INSERT INTO "auth_permission" VALUES(7,'Can delete group',2,'delete_group');
INSERT INTO "auth_permission" VALUES(8,'Can delete permission',1,'delete_permission');
INSERT INTO "auth_permission" VALUES(9,'Can delete user',3,'delete_user');
INSERT INTO "auth_permission" VALUES(10,'Can add content type',4,'add_contenttype');
INSERT INTO "auth_permission" VALUES(11,'Can change content type',4,'change_contenttype');
INSERT INTO "auth_permission" VALUES(12,'Can delete content type',4,'delete_contenttype');
INSERT INTO "auth_permission" VALUES(13,'Can add session',5,'add_session');
INSERT INTO "auth_permission" VALUES(14,'Can change session',5,'change_session');
INSERT INTO "auth_permission" VALUES(15,'Can delete session',5,'delete_session');
INSERT INTO "auth_permission" VALUES(16,'Can add site',6,'add_site');
INSERT INTO "auth_permission" VALUES(17,'Can change site',6,'change_site');
INSERT INTO "auth_permission" VALUES(18,'Can delete site',6,'delete_site');
INSERT INTO "auth_permission" VALUES(19,'Can add log entry',7,'add_logentry');
INSERT INTO "auth_permission" VALUES(20,'Can change log entry',7,'change_logentry');
INSERT INTO "auth_permission" VALUES(21,'Can delete log entry',7,'delete_logentry');
INSERT INTO "auth_permission" VALUES(22,'Can add author',9,'add_author');
INSERT INTO "auth_permission" VALUES(23,'Can add document',8,'add_document');
INSERT INTO "auth_permission" VALUES(24,'Can change author',9,'change_author');
INSERT INTO "auth_permission" VALUES(25,'Can change document',8,'change_document');
INSERT INTO "auth_permission" VALUES(26,'Can delete author',9,'delete_author');
INSERT INTO "auth_permission" VALUES(27,'Can delete document',8,'delete_document');
CREATE TABLE "auth_group_permissions" (
    "id" integer NOT NULL PRIMARY KEY,
    "group_id" integer NOT NULL,
    "permission_id" integer NOT NULL REFERENCES "auth_permission" ("id"),
    UNIQUE ("group_id", "permission_id")
);
CREATE TABLE "auth_group" (
    "id" integer NOT NULL PRIMARY KEY,
    "name" varchar(80) NOT NULL UNIQUE
);
CREATE TABLE "auth_user_user_permissions" (
    "id" integer NOT NULL PRIMARY KEY,
    "user_id" integer NOT NULL,
    "permission_id" integer NOT NULL REFERENCES "auth_permission" ("id"),
    UNIQUE ("user_id", "permission_id")
);
CREATE TABLE "auth_user_groups" (
    "id" integer NOT NULL PRIMARY KEY,
    "user_id" integer NOT NULL,
    "group_id" integer NOT NULL REFERENCES "auth_group" ("id"),
    UNIQUE ("user_id", "group_id")
);
CREATE TABLE "auth_user" (
    "id" integer NOT NULL PRIMARY KEY,
    "username" varchar(30) NOT NULL UNIQUE,
    "first_name" varchar(30) NOT NULL,
    "last_name" varchar(30) NOT NULL,
    "email" varchar(75) NOT NULL,
    "password" varchar(128) NOT NULL,
    "is_staff" bool NOT NULL,
    "is_active" bool NOT NULL,
    "is_superuser" bool NOT NULL,
    "last_login" datetime NOT NULL,
    "date_joined" datetime NOT NULL
);
INSERT INTO "auth_user" VALUES(1,'igor.soares','','','igor.soares@cds.com.br','pbkdf2_sha256$10000$4EfRvY4X6uqz$/b22EywNfcLzzCu3Ru5cNzvSHr4aBL1sRHIdamYmQa0=',1,1,1,'2012-04-27 12:47:54.394499','2012-04-27 12:47:26.186296');
CREATE TABLE "django_content_type" (
    "id" integer NOT NULL PRIMARY KEY,
    "name" varchar(100) NOT NULL,
    "app_label" varchar(100) NOT NULL,
    "model" varchar(100) NOT NULL,
    UNIQUE ("app_label", "model")
);
INSERT INTO "django_content_type" VALUES(1,'permission','auth','permission');
INSERT INTO "django_content_type" VALUES(2,'group','auth','group');
INSERT INTO "django_content_type" VALUES(3,'user','auth','user');
INSERT INTO "django_content_type" VALUES(4,'content type','contenttypes','contenttype');
INSERT INTO "django_content_type" VALUES(5,'session','sessions','session');
INSERT INTO "django_content_type" VALUES(6,'site','sites','site');
INSERT INTO "django_content_type" VALUES(7,'log entry','admin','logentry');
INSERT INTO "django_content_type" VALUES(8,'document','testapp','document');
INSERT INTO "django_content_type" VALUES(9,'author','testapp','author');
CREATE TABLE "django_session" (
    "session_key" varchar(40) NOT NULL PRIMARY KEY,
    "session_data" text NOT NULL,
    "expire_date" datetime NOT NULL
);
INSERT INTO "django_session" VALUES('cbac6f8ba6f473ea97b27f2405c7df00','ZTExODA0ODIwNGIzNTgzZWViNjdmYTY0MzhhN2IxMzE4ZGY2MzU0NjqAAn1xAShVEl9hdXRoX3Vz
ZXJfYmFja2VuZHECVSlkamFuZ28uY29udHJpYi5hdXRoLmJhY2tlbmRzLk1vZGVsQmFja2VuZHED
VQ1fYXV0aF91c2VyX2lkcQRLAXUu
','2012-05-11 12:47:54.401106');
CREATE TABLE "django_site" (
    "id" integer NOT NULL PRIMARY KEY,
    "domain" varchar(100) NOT NULL,
    "name" varchar(50) NOT NULL
);
INSERT INTO "django_site" VALUES(1,'example.com','example.com');
CREATE TABLE "django_admin_log" (
    "id" integer NOT NULL PRIMARY KEY,
    "action_time" datetime NOT NULL,
    "user_id" integer NOT NULL REFERENCES "auth_user" ("id"),
    "content_type_id" integer REFERENCES "django_content_type" ("id"),
    "object_id" text,
    "object_repr" varchar(200) NOT NULL,
    "action_flag" smallint unsigned NOT NULL,
    "change_message" text NOT NULL
);
INSERT INTO "django_admin_log" VALUES(1,'2012-04-27 12:48:41.652903',1,9,'1','Author object',1,'');
INSERT INTO "django_admin_log" VALUES(2,'2012-04-27 12:48:52.655925',1,9,'2','Author object',1,'');
INSERT INTO "django_admin_log" VALUES(3,'2012-04-27 12:50:27.672729',1,8,'1','Document object',1,'');
INSERT INTO "django_admin_log" VALUES(4,'2012-04-27 12:53:16.872986',1,8,'2','Outro Documento do primeiro autor',1,'');
INSERT INTO "django_admin_log" VALUES(5,'2012-04-27 12:53:44.613700',1,8,'3','Documento de outro autor',1,'');
CREATE TABLE "testapp_document" (
    "id" integer NOT NULL PRIMARY KEY,
    "title" varchar(79) NOT NULL,
    "text" text NOT NULL,
    "author_id" integer NOT NULL
);
INSERT INTO "testapp_document" VALUES(1,'Documento Teste','Texto com unicode (atenção)
e newlines',1);
INSERT INTO "testapp_document" VALUES(2,'Outro Documento do primeiro autor','Blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah',1);
INSERT INTO "testapp_document" VALUES(3,'Documento de outro autor','Outro  blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah',2);
CREATE TABLE "testapp_author" (
    "id" integer NOT NULL PRIMARY KEY,
    "name" varchar(79) NOT NULL UNIQUE
);
INSERT INTO "testapp_author" VALUES(1,'Autor Teste');
INSERT INTO "testapp_author" VALUES(2,'Outro Autor');
CREATE INDEX "auth_permission_e4470c6e" ON "auth_permission" ("content_type_id");
CREATE INDEX "auth_group_permissions_bda51c3c" ON "auth_group_permissions" ("group_id");
CREATE INDEX "auth_group_permissions_1e014c8f" ON "auth_group_permissions" ("permission_id");
CREATE INDEX "auth_user_user_permissions_fbfc09f1" ON "auth_user_user_permissions" ("user_id");
CREATE INDEX "auth_user_user_permissions_1e014c8f" ON "auth_user_user_permissions" ("permission_id");
CREATE INDEX "auth_user_groups_fbfc09f1" ON "auth_user_groups" ("user_id");
CREATE INDEX "auth_user_groups_bda51c3c" ON "auth_user_groups" ("group_id");
CREATE INDEX "django_session_c25c2c28" ON "django_session" ("expire_date");
CREATE INDEX "django_admin_log_fbfc09f1" ON "django_admin_log" ("user_id");
CREATE INDEX "django_admin_log_e4470c6e" ON "django_admin_log" ("content_type_id");
CREATE INDEX "testapp_document_cc846901" ON "testapp_document" ("author_id");
COMMIT;
