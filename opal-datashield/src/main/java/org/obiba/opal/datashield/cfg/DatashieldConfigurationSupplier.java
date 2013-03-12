/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datashield.cfg;

import org.obiba.opal.core.cfg.ExtensionConfigurationSupplier;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatashieldConfigurationSupplier extends ExtensionConfigurationSupplier<DatashieldConfiguration> {

  @Autowired
  public DatashieldConfigurationSupplier(OpalConfigurationService opalConfigurationService) {
    super(opalConfigurationService, DatashieldConfiguration.class);
  }

}
