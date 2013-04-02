/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search.es;

import org.obiba.opal.core.cfg.ExtensionConfigurationSupplier;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ElasticSearchConfigurationService {

  private final ExtensionConfigurationSupplier<ElasticSearchConfiguration> configSupplier;

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Autowired
  public ElasticSearchConfigurationService(OpalConfigurationService configService) {
    configSupplier = new ExtensionConfigurationSupplier<ElasticSearchConfiguration>(configService,
        ElasticSearchConfiguration.class);
  }

  public ElasticSearchConfiguration getConfig() {
    if(!configSupplier.hasExtension()) {
      configSupplier.addExtension(new ElasticSearchConfiguration());
    }
    return configSupplier.get();
  }

  public void update(ElasticSearchConfiguration config) {
    // persist using the config supplier
    configSupplier.addExtension(config);

    // restart ES?
  }
}
