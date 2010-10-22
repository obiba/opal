The scripts in this directory are from the "docs/dbTables" directory of the Quartz distribution, version 1.8.3.

The Opal Upgrade Manager selects the script to be executed based on the runtime database product. For details,
refer to SqlScriptUpgradeStep.java (in the package org.obiba.runtime.upgrade.support.jdbc).

IMPORTANT NOTE
==============

The original scripts from the Quartz distribution have been modified as follows:
  1. All comments have been removed, due to the script parser's mishandling of comments (see http://jira.obiba.org/jira/browse/COM-5).
  
  2. Some scripts have been renamed, for compatibility with the DatabaseProductRegistry class (specifically, with the latter's 
     list of database products in database-products.xml).
     
     Original script		Renamed as
     ---------------    	----------
     
     tables_hsqldb*.sql 	tables_hsql*.sql
     tables_postgres*.sql	tables_pgsql*.sql
     tables_sqlServer.sql	tables_sqlserver.sql
     
     
     