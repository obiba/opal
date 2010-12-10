ALTER TABLE `value_set_value`
  DROP COLUMN `id`,  
  DROP INDEX `value_set_id`, 
  ADD PRIMARY KEY (`value_set_id`,`variable_id`);
