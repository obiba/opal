/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.resource.impl;

import com.google.common.collect.Lists;
import org.obiba.opal.spi.resource.ResourceFactory;
import org.obiba.opal.spi.resource.ResourceFactoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public abstract class AbstractResourceFactoryService implements ResourceFactoryService {

  private static final Logger log = LoggerFactory.getLogger(AbstractResourceFactoryService.class);

  private static final String RESOURCES_DIR = "resources";

  private Properties properties;

  private boolean running;

  private List<ResourceFactory> resourceFactories;

  @Override
  public Properties getProperties() {
    return properties;
  }

  @Override
  public void configure(Properties properties) {
    this.properties = properties;
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void start() {
    running = true;
    resourceFactories = initResourceFactories();
  }

  @Override
  public void stop() {
    running = false;
  }

  @Override
  public List<ResourceFactory> getResourceFactories() {
    return resourceFactories;
  }

  private List<ResourceFactory> initResourceFactories() {
    Path resourcesDirectoryPath = Paths.get(getProperties().getProperty(INSTALL_DIR_PROPERTY), RESOURCES_DIR)
        .toAbsolutePath();
    if (Files.isDirectory(resourcesDirectoryPath)) {
      try {
        return Files.list(resourcesDirectoryPath).filter(p -> Files.isDirectory(p))
            .map(p -> new DefaultResourceFactory(p.toFile()))
            .collect(Collectors.toList());
      } catch (IOException e) {
        log.error("No resources directory.");
      }
    }

    return Lists.newArrayList();
  }
}
