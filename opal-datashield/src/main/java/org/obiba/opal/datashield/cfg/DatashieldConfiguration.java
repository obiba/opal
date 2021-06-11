/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.datashield.cfg;

import org.obiba.datashield.core.DSEnvironment;
import org.obiba.datashield.core.DSMethodType;
import org.obiba.datashield.core.impl.DefaultDSConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.datashield.DataShieldEnvironment;
import org.obiba.opal.web.model.DataShield;

import java.io.Serializable;

@Deprecated
public class DatashieldConfiguration extends DefaultDSConfiguration implements OpalConfigurationExtension, Serializable {

  @Deprecated
  private String level;

  @Override
  public synchronized DSEnvironment getEnvironment(DSMethodType type) {
    if (level != null) {
      level = null;
    }
    return super.getEnvironment(type);
  }

  public void addOptions(Iterable<DataShield.DataShieldROptionDto> optionsList) {
    for (DataShield.DataShieldROptionDto option : optionsList) {
      addOption(option.getName(), option.getValue(), false);
    }
  }

  @Override
  protected DSEnvironment makeEnvironment(DSMethodType type) {
    return new DataShieldEnvironment(type);
  }
}
