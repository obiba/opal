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
import javax.validation.constraints.NotNull;

import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableUpdateListener;
import org.obiba.magma.Variable;
import org.obiba.opal.core.cfg.ExtensionConfigurationSupplier;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IndexManagerConfigurationService implements ValueTableUpdateListener {

  private final ExtensionConfigurationSupplier<IndexManagerConfiguration> configSupplier;

  @Autowired
  public IndexManagerConfigurationService(OpalConfigurationService configService) {
    configSupplier = new ExtensionConfigurationSupplier<>(configService, IndexManagerConfiguration.class);
  }

  public IndexManagerConfiguration getConfig() {
    if(!configSupplier.hasExtension()) {
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
   * Enable this index.
   * @param enabled
   */
  public void setEnabled(boolean enabled) {
    IndexManagerConfiguration config = getConfig();
    config.setEnabled(enabled);
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
  public void onRename(@NotNull ValueTable vt, String newName) {
    onDelete(vt);
  }

  @Override
  public void onRename(@Nonnull ValueTable vt, Variable v, String newName) {
    onDelete(vt);
  }

  @Override
  public void onDelete(@NotNull ValueTable vt) {
    remove(vt);
  }
}
