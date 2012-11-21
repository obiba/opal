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

import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.web.model.Opal;

/**
 * A service configuration handler to get a {@code Opal.ServiceCfgDto} given an {@code OpalConfigurationExtension}
 * and to update a {@code OpalConfigurationExtension} for a given {@code Opal.ServiceCfgDto} .
 */
public interface ServiceConfigurationHandler {

  /**
   * Parses the provided {@code OpalConfigurationExtension} instance and builds a corresponding {@code
   * Opal.ServiceCfgDto}
   * instance.
   *
   * @param config
   * @return the service dto
   */
  public Opal.ServiceCfgDto get(OpalConfigurationExtension config);

  /**
   * Parses the provided {@code Opal.ServiceCfgDto} instance and builds a corresponding {@code
   * OpalConfigurationExtension}
   * instance.
   *
   * @param serviceDto
   */
  public void put(Opal.ServiceCfgDto serviceDto);

  /**
   * Returns true when this instance is capable of building {@code OpalConfigurationExtension} and convert it into a
   * {@code Opal.ServiceCfgDto}
   *
   * @param config
   * @return
   */
  public boolean canGet(OpalConfigurationExtension config);

  /**
   * Returns true when this instance is capable of building {@code Opal.ServiceCfgDto} and convert it into a
   * {@code OpalConfigurationExtension}
   *
   * @param serviceDto
   * @return
   */
  public boolean canPut(Opal.ServiceCfgDto serviceDto);

}
