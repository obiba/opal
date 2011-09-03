CREATE PROCEDURE update_var_index()
MODIFIES SQL DATA
BEGIN ATOMIC
  DECLARE done, i INT DEFAULT 0;

  table_loop: FOR SELECT id as tid from value_table DO
    SET i = 0;
    var_loop: FOR SELECT id as vid from variable WHERE value_table_id = tid ORDER BY variable.id DO
      UPDATE variable SET variable_index = i WHERE id = vid;
      SET i = i + 1;
    END FOR;
  END FOR;
END

