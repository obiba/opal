The scripts in this directory are from the "docs/dbTables" directory of the Quartz distribution, version 1.8.3.

NOTE: Due to mishandling of script comments (see http://jira.obiba.org/jira/browse/COM-5), comments present in
the original scripts have been removed.

The Opal Upgrade Manager selects the script to be executed based on the runtime database product. For details,
refer to SqlScriptUpgradeStep.java (in the package org.obiba.runtime.upgrade.support.jdbc).