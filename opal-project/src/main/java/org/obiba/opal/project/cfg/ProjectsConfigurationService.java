/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.project.cfg;

import org.obiba.opal.core.cfg.ExtensionConfigurationSupplier;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProjectsConfigurationService {
  private final ExtensionConfigurationSupplier<ProjectsConfiguration> configSupplier;

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Autowired
  public ProjectsConfigurationService(OpalConfigurationService configService) {
    configSupplier = new ExtensionConfigurationSupplier<ProjectsConfiguration>(configService,
        ProjectsConfiguration.class);
  }

  public ProjectsConfiguration getConfig() {
    if(!configSupplier.hasExtension()) {
      configSupplier.addExtension(new ProjectsConfiguration());
    }
    return configSupplier.get();
  }

  public void update(ProjectsConfiguration config) {
    // persist using the config supplier
    configSupplier.addExtension(config);
  }
}
