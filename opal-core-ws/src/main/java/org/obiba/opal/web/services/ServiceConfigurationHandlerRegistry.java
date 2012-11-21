/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.services;

import java.util.Set;

import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.web.model.Opal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Registry of {@code ServiceConfigurationHandler}
 */
@Component
public class ServiceConfigurationHandlerRegistry {

  private final Set<ServiceConfigurationHandler> handlers;

  @Autowired
  public ServiceConfigurationHandlerRegistry(Set<ServiceConfigurationHandler> handlers) {
    if(handlers == null) throw new IllegalArgumentException("handlers cannot be null");
    this.handlers = handlers;
  }

  /**
   * Parses the provided {@code OpalConfigurationExtension} instance and returns a {@code Opal.ServiceCfgDto}
   *
   * @param configExtension the {@code OpalConfigurationExtension} to parse
   * @param name the name of the service
   * @return a {@code Opal.ServiceCfgdto}
   * @throws org.obiba.opal.core.runtime.NoSuchServiceConfigurationException when no {@code OpalConfigurationExtension} is available
   */
  public Opal.ServiceCfgDto get(OpalConfigurationExtension configExtension,
      String name) throws NoSuchServiceConfigurationException {
    if(configExtension == null) throw new IllegalArgumentException("configExtension cannot be null");
    for(ServiceConfigurationHandler handler : handlers) {
      if(handler.canGet(configExtension)) {
        return handler.get(configExtension);
      }
    }
    throw new NoSuchServiceConfigurationException("No configuration for service: " + name);
  }

  /**
   * Parses the provided {@code Opal.ServiceCfgDto} instance and returns a {@code OpalConfigurationExtension}
   *
   * @param serviceDto the {@code Opal.ServiceCfgDto} to parse
   * @param name the name of the service
   * @return a {@code OpalConfigurationExtension}
   */
  public void put(Opal.ServiceCfgDto serviceDto, String name) throws RuntimeException {
    if(serviceDto == null) throw new IllegalArgumentException("serviceDto cannot be null");
    for(ServiceConfigurationHandler handler : handlers) {
      if(handler.canPut(serviceDto)) {
        handler.put(serviceDto);
      }
    }
  }

}
