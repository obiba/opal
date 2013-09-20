/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime.install;

import java.util.Arrays;

import org.obiba.opal.core.domain.server.OpalGeneralConfig;
import org.obiba.opal.core.service.impl.DefaultGeneralConfigService;
import org.w3c.dom.Document;

public class CreateOpalGeneralConfigInstallStep extends AbstractConfigurationInstallStep {

  private DefaultGeneralConfigService generalConfigService;

  public void setGeneralConfigService(DefaultGeneralConfigService generalConfigService) {
    this.generalConfigService = generalConfigService;
  }

  @Override
  public String getDescription() {
    return "Generate default configuration.";
  }

  @Override
  protected void doWithConfig(Document opalConfig) {
    OpalGeneralConfig conf = new OpalGeneralConfig();
    conf.setLocales(Arrays.asList("en"));

    generalConfigService.createServerConfig(conf);
  }

}
