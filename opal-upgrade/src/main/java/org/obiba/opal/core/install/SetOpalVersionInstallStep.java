/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.install;

import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.InstallStep;
import org.springframework.beans.factory.annotation.Autowired;

public class SetOpalVersionInstallStep implements InstallStep {

  @Autowired
  private OpalConfigurationService configurationService;

  @Override
  public void execute(final Version currentVersion) {
    configurationService.modifyConfiguration(new OpalConfigurationService.ConfigModificationTask() {
      @Override
      public void doWithConfig(OpalConfiguration config) {
        config.setVersion(currentVersion);
      }
    });
  }

  @Override
  @SuppressWarnings("MethodReturnAlwaysConstant")
  public String getDescription() {
    return "Set current version to Opal Configuration";
  }

}
