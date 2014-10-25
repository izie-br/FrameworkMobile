ALTER TABLE tb_document ADD COLUMN inactivated_at timestamp NULL;
ALTER TABLE tb_author ADD COLUMN inactivated_at timestamp NULL;
ALTER TABLE tb_score ADD COLUMN inactivated_at timestamp NULL;
ALTER TABLE tb_customer ADD COLUMN inactivated_at timestamp NULL;
