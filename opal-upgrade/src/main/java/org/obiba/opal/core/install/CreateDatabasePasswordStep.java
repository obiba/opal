/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.install;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.service.LocalOrientDbServerFactory;
import org.obiba.opal.core.service.OrientDbServerFactory;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.InstallStep;
import org.obiba.runtime.upgrade.UpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;

public class CreateDatabasePasswordStep implements InstallStep, UpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(CreateDatabasePasswordStep.class);

  @Autowired
  private OpalConfigurationService configurationService;

  @Autowired
  private OrientDbServerFactory orientDbServerFactory;

  @Override
  public String getDescription() {
    return "Generate a password for config databases specific to this Opal instance.";
  }

  @Override
  @SuppressWarnings("MagicNumber")
  public void execute(Version currentVersion) {
    final String password = new BigInteger(130, new SecureRandom()).toString(32);
//    final String password = "1234";
    ODatabaseDocumentTx database = orientDbServerFactory.getDocumentTx();
    try {
      log.info(">>> BEFORE");
      debug(database);
      database.begin();
      database.command(new OCommandSQL(
          "update ouser set password = '" + password + "' where name = '" + LocalOrientDbServerFactory.USERNAME + "'"))
          .execute();
      database.command(new OCommandSQL(
          "update ouser set status= 'SUSPENDED' where name <> '" + LocalOrientDbServerFactory.USERNAME + "'"))
          .execute();
      database.commit();
      log.info(">>> AFTER");
      debug(database);
      configurationService.modifyConfiguration(new OpalConfigurationService.ConfigModificationTask() {
        @Override
        public void doWithConfig(OpalConfiguration config) {
          config.setDatabasePassword(password);
        }
      });
    } catch(Exception e) {
      database.rollback();
      throw new RuntimeException(e);
    } finally {
      database.close();
    }
  }

  @Override
  public Version getAppliesTo() {
    return new Version(2, 0, 0);
  }

  private void debug(ODatabaseDocument database) {
    for(ODocument ouser : database.browseClass("ouser")) {
      log.info("ouser: {}", ouser.toJSON());
    }
  }
}
