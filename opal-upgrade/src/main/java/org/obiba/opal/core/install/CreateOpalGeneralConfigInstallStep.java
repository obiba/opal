/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.install;

import java.util.Arrays;

import org.obiba.opal.core.domain.OpalGeneralConfig;
import org.obiba.opal.core.service.impl.DefaultGeneralConfigService;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.InstallStep;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("SpringJavaAutowiringInspection")
public class CreateOpalGeneralConfigInstallStep implements InstallStep {

  @Autowired
  private DefaultGeneralConfigService generalConfigService;

  @Override
  public String getDescription() {
    return "Generate and store default configuration.";
  }

  @Override
  public void execute(Version currentVersion) {
    OpalGeneralConfig conf = new OpalGeneralConfig();
    conf.setLocales(Arrays.asList(OpalGeneralConfig.DEFAULT_LOCALE));
    generalConfigService.save(conf);
  }

}
