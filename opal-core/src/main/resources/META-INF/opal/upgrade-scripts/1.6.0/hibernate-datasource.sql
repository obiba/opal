ALTER TABLE variable DROP COLUMN pos;
ALTER TABLE variable ADD COLUMN variable_index int NOT NULL DEFAULT 0;
