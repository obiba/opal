CREATE TABLE `value_set_binary_value` (
  `id`           BIGINT(20) NOT NULL AUTO_INCREMENT,
  `occurrence`   INT(11)    NOT NULL,
  `size`         INT(11)    NOT NULL,
  `value`        LONGBLOB,
  `value_set_id` BIGINT(20) NOT NULL,
  `variable_id`  BIGINT(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `value_set_id` (`value_set_id`, `variable_id`, `occurrence`),
  KEY `occurrenceIndex` (`occurrence`),
  KEY `variableIndex` (`variable_id`),
  KEY `valueSetIndex` (`value_set_id`),
  CONSTRAINT `valueSetIndex` FOREIGN KEY (`value_set_id`) REFERENCES `value_set` (`id`),
  CONSTRAINT `variableIndex` FOREIGN KEY (`variable_id`) REFERENCES `variable` (`id`)
) ENGINE = InnoDB;
