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

import com.google.common.collect.Lists;
import org.obiba.datashield.core.DSConfiguration;
import org.obiba.datashield.core.impl.DefaultDSConfiguration;
import org.obiba.opal.core.domain.HasUniqueProperties;
import org.obiba.opal.web.model.DataShield;

import java.util.List;

public class DatashieldConfig extends DefaultDSConfiguration implements DSConfiguration, HasUniqueProperties {

  private String profile;

  public DatashieldConfig() {}

  public DatashieldConfig(String profile) {
    this.profile = profile;
  }

  public String getProfile() {
    return profile;
  }

  public void setProfile(String profile) {
    this.profile = profile;
  }

  public void addOptions(Iterable<DataShield.DataShieldROptionDto> optionsList) {
    for (DataShield.DataShieldROptionDto option : optionsList) {
      addOption(option.getName(), option.getValue(), false);
    }
  }

  @Override
  public List<String> getUniqueProperties() {
    return Lists.newArrayList("profile");
  }

  @Override
  public List<Object> getUniqueValues() {
    return Lists.newArrayList(profile);
  }
}
