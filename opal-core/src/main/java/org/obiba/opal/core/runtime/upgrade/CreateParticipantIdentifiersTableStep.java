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

import java.io.IOException;

import org.hibernate.SessionFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.datasource.hibernate.HibernateDatasource;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.InstallStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 *
 */
public class CreateParticipantIdentifiersTableStep implements InstallStep {

  @Autowired
  @Value("${org.obiba.opal.keys.tableReference}")
  private String keysTableReference;

  @Autowired
  private PlatformTransactionManager txMgr;

  private SessionFactory keysSession;

  @Override
  public void execute(Version currentVersion) {

    new TransactionTemplate(txMgr).execute(new TransactionCallbackWithoutResult() {

      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf(keysTableReference);
          Datasource ds = new MagmaEngine().addDatasource(new HibernateDatasource(resolver.getDatasourceName(), keysSession));
          try {
            if(ds.hasValueTable(resolver.getTableName()) == false) {
              ds.createWriter(resolver.getTableName(), "Participant").close();
            }
          } catch(IOException e) {
            throw new RuntimeException(e);
          }
        } finally {
          try {
            MagmaEngine.get().shutdown();
          } catch(RuntimeException e) {

          }
        }
      }
    });
  }

  @Override
  public String getDescription() {
    return "Creates the identifier table in the identifier datasource if it does not exist";
  }

  public void setKeysSessionFactory(SessionFactory keysSession) {
    this.keysSession = keysSession;
  }
}
