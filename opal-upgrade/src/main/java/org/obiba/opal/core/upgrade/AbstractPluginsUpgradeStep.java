/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.upgrade;

import org.obiba.opal.core.cfg.PluginsService;
import org.obiba.plugins.PluginPackage;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractPluginsUpgradeStep extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(AbstractPluginsUpgradeStep.class);

  @Autowired
  private PluginsService pluginsService;

  @Override
  public void execute(Version version) {
    try {
      log.info("Checking for plugins to upgrade");
      for (PluginPackage updatablePlugin : pluginsService.getUpdatablePlugins()) {
        if (applyUpgrade(updatablePlugin)) {
          log.info("Installing plugin {} {}...", updatablePlugin.getName(), updatablePlugin.getVersion().toString());
          pluginsService.installPlugin(updatablePlugin.getName(), updatablePlugin.getVersion().toString());
        }
      }
    } catch (Exception e) {
      log.error("Plugin upgrade failure", e);
    }
  }

  protected abstract boolean applyUpgrade(PluginPackage updatablePlugin);
}
