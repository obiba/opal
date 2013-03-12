CREATE PROCEDURE update_var_index()
  BEGIN
    DECLARE done, i INT DEFAULT 0;
    DECLARE t, v BIGINT;
    DECLARE tables CURSOR FOR SELECT
                                id
                              FROM value_table;
    DECLARE vars CURSOR FOR SELECT
                              id
                            FROM variable
                            WHERE value_table_id = t
                            ORDER BY id;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    OPEN tables;
      table_loop: LOOP
      FETCH tables
      INTO t;
    IF done
    THEN LEAVE table_loop; END IF;

      OPEN vars;
      var_loop: LOOP
      FETCH vars
      INTO v;
    IF done
    THEN LEAVE var_loop; END IF;
    UPDATE variable
    SET variable_index = i
    WHERE id = v;
    SET i = i + 1;
    END LOOP;
      CLOSE vars;
    SET done = 0;
    SET i = 0;
    END LOOP;
    CLOSE tables;
  END
