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

import org.obiba.opal.core.cfg.PluginsService;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.InstallStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultPluginsInstallStep implements InstallStep {

  private static final Logger log = LoggerFactory.getLogger(DefaultPluginsInstallStep.class);

  @Autowired
  private PluginsService pluginsService;

  @Override
  public String getDescription() {
    return "Install the default plugins from the OBiBa plugins repository.";
  }

  @Override
  public void execute(Version currentVersion) {
    log.info("No default plugin is to be installed from update site: {}", pluginsService.getUpdateSite());
  }
}
