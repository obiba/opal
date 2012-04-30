update subject_acl 
set 
  domain='opal', permission='DATABASES_ALL'
where 
  domain='magma' and
  node='/jdbc/databases' and 
  permission='*:POST/*';

update subject_acl 
set 
  domain='opal', permission='R_SESSION_ALL'
where 
  domain='magma' and
  node='/r/session' and 
  permission='*:GET/*';

update subject_acl 
set 
  domain='opal', permission='DATASHIELD_ALL'
where 
  domain='magma' and
  node='/datashield' and 
  permission='*:GET/*';
  
update subject_acl 
set 
  domain='opal', permission='DATASHIELD_SESSION_ALL'
where 
  domain='magma' and
  node='/datashield/session' and 
  permission='*:GET/*';
  
delete from subject_acl 
where node like '%/summary';

delete from subject_acl 
where node like '%/valueSets';

update subject_acl 
set 
  domain='opal', permission='VARIABLE_READ'
where 
  domain='magma' and
  node like '/datasource/%/table/%/variable/%' and 
  permission='GET:GET';

update subject_acl 
set 
  domain='opal', permission='TABLE_READ'
where 
  domain='magma' and
  node like '/datasource/%/table/%' and 
  permission='GET:GET';

delete from subject_acl 
where 
  domain='magma' and
  (node like '/datasource/%/table/%' or node like '/datasource/%/view/%');
  
update subject_acl 
set 
  domain='opal', permission='DATASOURCE_ALL'
where 
  domain='magma' and
  node like '/datasource/%' and 
  permission='GET:GET/GET';

delete from subject_acl 
where domain='magma';