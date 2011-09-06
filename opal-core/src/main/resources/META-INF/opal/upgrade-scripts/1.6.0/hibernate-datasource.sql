ALTER TABLE variable DROP COLUMN pos;
ALTER TABLE variable ADD COLUMN variable_index int DEFAULT 0 NOT NULL;
