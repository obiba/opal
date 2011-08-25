ALTER TABLE variable 
	DROP COLUMN pos,
	ADD COLUMN `variable_index` int(11) NOT NULL;
DROP PROCEDURE IF EXISTS update_var_index;
