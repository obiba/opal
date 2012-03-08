/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.cfg;

import org.obiba.opal.core.cfg.OpalConfigurationService.ConfigModificationTask;

import com.google.common.base.Supplier;

/**
 * A base class for managing a {@code OpalConfigurationExtension} within the {@code OpalConfiguraton}. Simply extend
 * this class and provide the concrete implementation of {@code OpalConfigurationExtension}.
 * 
 * @param <T> the concrete implementation of {@code OpalConfigurationExtension} that is managed by this instance.
 */
public abstract class ExtensionConfigurationSupplier<T extends OpalConfigurationExtension> implements Supplier<T> {

  private final OpalConfigurationService opalConfigurationService;

  private final Class<T> extensionType;

  protected ExtensionConfigurationSupplier(OpalConfigurationService opalConfigurationService, Class<T> extensionType) {
    this.opalConfigurationService = opalConfigurationService;
    this.extensionType = extensionType;
  }

  public boolean hasExtension() {
    return opalConfigurationService.getOpalConfiguration().hasExtension(extensionType);
  }

  public void addExtension(final T extensionConfig) {
    opalConfigurationService.modifyConfiguration(new ConfigModificationTask() {

      @Override
      public void doWithConfig(OpalConfiguration config) {
        config.addExtension(extensionConfig);
      }
    });
  }

  /**
   * Returns the {@code OpalConfigurationExtension} implementation from the {@code OpalConfiguration}
   */
  @Override
  public T get() {
    return opalConfigurationService.getOpalConfiguration().getExtension(extensionType);
  }

  /**
   * Use this method to persist modifications to the configuration.
   * @param task the callback that will do the actual modifications.
   */
  public void modify(final ExtensionConfigModificationTask<T> task) {
    opalConfigurationService.modifyConfiguration(new ConfigModificationTask() {

      @Override
      public void doWithConfig(OpalConfiguration config) {
        task.doWithConfig(config.getExtension(extensionType));
      }
    });
  }

  public interface ExtensionConfigModificationTask<T extends OpalConfigurationExtension> {
    public void doWithConfig(T config);
  }

}
