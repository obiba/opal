ALTER TABLE `value_set_value` DROP COLUMN `id`;  
ALTER TABLE `value_set_value` DROP INDEX `value_set_id`; 
ALTER TABLE `value_set_value` ADD PRIMARY KEY (`value_set_id`,`variable_id`);
