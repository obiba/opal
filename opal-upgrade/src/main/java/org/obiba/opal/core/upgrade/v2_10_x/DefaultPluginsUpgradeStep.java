/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.upgrade.v2_10_x;

import org.obiba.opal.core.cfg.PluginsService;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultPluginsUpgradeStep extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(DefaultPluginsUpgradeStep.class);

  private static final String OPAL_SEARCH_PLUGIN = "opal-search-es";

  @Autowired
  private PluginsService pluginsService;

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
