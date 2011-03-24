/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime.upgrade;

import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.InstallStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 *
 */
public class CreateParticipantIdentifiersTableStep implements InstallStep {

  @Autowired
  private IdentifiersTableService identifiersTableService;

  @Autowired
  private PlatformTransactionManager txMgr;

  @Override
  public void execute(Version currentVersion) {

    new TransactionTemplate(txMgr).execute(new TransactionCallbackWithoutResult() {

      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        identifiersTableService.start();
      }
    });
  }

  @Override
  public String getDescription() {
    return "Creates the identifier table in the identifier datasource if it does not exist";
  }

}
