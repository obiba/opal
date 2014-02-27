The scripts in this directory are from the "docs/dbTables" directory of the Quartz distribution, version 2.2.1.

Script tables_hsqldb.sql was renamed to tables_hsql.sql for compatibility with the DatabaseProductRegistry class
(specifically, with the latter's list of database products in database-products.xml).

The Opal Upgrade Manager selects the script to be executed based on the runtime database product. For details,
refer to SqlScriptUpgradeStep.java (in the package org.obiba.runtime.upgrade.support.jdbc).
