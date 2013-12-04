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
import org.obiba.opal.core.service.OrientDbServerFactory;
import org.obiba.opal.core.service.impl.LocalOrientDbServerFactory;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.InstallStep;
import org.obiba.runtime.upgrade.UpgradeStep;
import org.springframework.beans.factory.annotation.Autowired;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.sql.OCommandSQL;

public class CreateDatabasePasswordInstallStep implements InstallStep, UpgradeStep {

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
    configurationService.modifyConfiguration(new OpalConfigurationService.ConfigModificationTask() {
      @Override
      public void doWithConfig(OpalConfiguration config) {
        config.setDatabasePassword(password);
      }
    });

    ODatabaseDocumentTx database = orientDbServerFactory.getDocumentTx();
    try {
      database.command(new OCommandSQL(
          "update ouser set password = '" + password + "' where name = '" + LocalOrientDbServerFactory.USERNAME + "'"))
          .execute();
      database.command(new OCommandSQL(
          "update ouser set status= 'SUSPENDED' where name <> '" + LocalOrientDbServerFactory.USERNAME + "'"))
          .execute();
    } finally {
      database.close();
    }
  }

  @Override
  public Version getAppliesTo() {
    return new Version(2, 0, 0);
  }
}
