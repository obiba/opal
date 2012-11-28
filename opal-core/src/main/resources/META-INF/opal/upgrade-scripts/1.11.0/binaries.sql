ALTER TABLE `value_set_value`
  ADD COLUMN `id` bigint(20) NOT NULL AUTO_INCREMENT FIRST,
  DROP PRIMARY KEY,
  ADD PRIMARY KEY (`id`);

CREATE TABLE `value_set_binary_value` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created` datetime NOT NULL,
  `updated` datetime NOT NULL,
  `occurrence` int(11) NOT NULL,
  `size` int(11) NOT NULL,
  `value` longblob,
  `value_set_value_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `value_set_value_id` (`value_set_value_id`,`occurrence`),
  KEY `occurrenceIndex` (`occurrence`),
  KEY `FKB3B8597E3B500505` (`value_set_value_id`),
  CONSTRAINT `FKB3B8597E3B500505` FOREIGN KEY (`value_set_value_id`) REFERENCES `value_set_value` (`id`)
);


