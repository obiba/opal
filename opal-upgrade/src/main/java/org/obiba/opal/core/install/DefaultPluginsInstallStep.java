/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.install;

import org.obiba.opal.core.cfg.PluginsService;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.InstallStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultPluginsInstallStep implements InstallStep {

  private static final Logger log = LoggerFactory.getLogger(DefaultPluginsInstallStep.class);

  private static final String OPAL_SEARCH_PLUGIN = "opal-search-es";

  @Autowired
  private PluginsService pluginsService;

  @Override
  public String getDescription() {
    return "Install the default plugins from the OBiBa plugins repository.";
  }

  @Override
  public void execute(Version currentVersion) {
    log.info("Installing default plugins from update site: {}", pluginsService.getUpdateSite());
    try {
      log.info("Installing {} plugin", OPAL_SEARCH_PLUGIN);
      pluginsService.installPlugin(OPAL_SEARCH_PLUGIN, null);
    } catch (Exception e) {
      log.error("Failed to install " + OPAL_SEARCH_PLUGIN + " plugin", e);
    }
  }
}
