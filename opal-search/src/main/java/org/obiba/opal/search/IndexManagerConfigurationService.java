/*
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search;

import javax.annotation.Nonnull;

import org.obiba.magma.ValueTable;
import org.obiba.opal.core.cfg.ExtensionConfigurationSupplier;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.web.magma.ValueTableUpdateListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IndexManagerConfigurationService implements ValueTableUpdateListener {

  private final ExtensionConfigurationSupplier<IndexManagerConfiguration> configSupplier;

  @Autowired
  public IndexManagerConfigurationService(OpalConfigurationService configService,
      ExtensionConfigurationSupplier configSupplier) {
    this.configSupplier = new ExtensionConfigurationSupplier<IndexManagerConfiguration>(configService,
        IndexManagerConfiguration.class);
  }

  public IndexManagerConfiguration getConfig() {
    if(configSupplier.hasExtension() == false) {
      configSupplier.addExtension(new IndexManagerConfiguration());
    }
    return configSupplier.get();
  }

  /**
   * Update schedule entry in index manager configuration.
   */
  public void update(ValueTable vt, Schedule schedule) {
    // change the config and persist using the config supplier
    IndexManagerConfiguration config = getConfig();
    config.updateSchedule(vt, schedule);
    configSupplier.addExtension(config);
  }

  /**
   * Removes schedule entry in index manager configuration. If table still exists, default scheduling behaviour applies.
   */
  public void remove(ValueTable vt) {
    // change the config and persist using the config supplier
    IndexManagerConfiguration config = getConfig();
    config.removeSchedule(vt);
    configSupplier.addExtension(config);
  }

  @Override
  public void onDelete(@Nonnull ValueTable vt) {
    remove(vt);
  }
}
