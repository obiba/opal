ALTER TABLE variable
DROP COLUMN pos,
ADD COLUMN `variable_index` INT(11) NOT NULL;
DROP PROCEDURE IF EXISTS update_var_index;
