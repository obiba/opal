CREATE TABLE `value_set_binary_value` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `occurrence` int(11) NOT NULL,
  `size` int(11) NOT NULL,
  `value` longblob,
  `value_set_id` bigint(20) NOT NULL,
  `variable_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `value_set_id` (`value_set_id`,`variable_id`,`occurrence`),
  KEY `occurrenceIndex` (`occurrence`),
  KEY `variableIndex` (`variable_id`),
  KEY `valueSetIndex` (`value_set_id`),
  CONSTRAINT `valueSetIndex` FOREIGN KEY (`value_set_id`) REFERENCES `value_set` (`id`),
  CONSTRAINT `variableIndex` FOREIGN KEY (`variable_id`) REFERENCES `variable` (`id`)
);
