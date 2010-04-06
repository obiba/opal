INSERT INTO unit_key_store (unit, key_store)
  SELECT 'OpalInstance', java_key_store
  FROM study_key_store
  WHERE study_id = 'DEFAULT_STUDY_ID';


DROP TABLE study_key_store;
