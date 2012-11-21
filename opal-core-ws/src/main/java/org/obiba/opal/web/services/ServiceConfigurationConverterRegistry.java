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
 *
 */
@Component
public class ServiceConfigurationConverterRegistry {

  private final Set<ServiceConfigurationConverter> converters;

  @Autowired
  public ServiceConfigurationConverterRegistry(Set<ServiceConfigurationConverter> converters) {
    if(converters == null) throw new IllegalArgumentException("converters cannot be null");
    this.converters = converters;
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
    for(ServiceConfigurationConverter converter : converters) {
      if(converter.canGet(configExtension)) {
        return converter.get(configExtension, name);
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
    for(ServiceConfigurationConverter converter : converters) {
      if(converter.canPut(serviceDto)) {
        converter.put(serviceDto);
      }
    }
  }

}
