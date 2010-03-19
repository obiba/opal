INSERT INTO unit_key_store (unit, key_store)
  SELECT 'OpalInstance', key_store
  FROM study_key_store
  WHERE unit = 'DEFAULT_STUDY_ID';


DROP TABLE study_key_store;