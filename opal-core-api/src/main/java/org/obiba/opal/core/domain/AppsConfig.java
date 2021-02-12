/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.domain;

import com.google.common.collect.Lists;

import java.util.List;

public class AppsConfig  implements HasUniqueProperties {

  private String id = "1";

  private String token;

  private List<RockAppConfig> rockAppConfigs = Lists.newArrayList();

  public String getId() {
    return id;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getToken() {
    return token;
  }

  public List<RockAppConfig> getRockAppConfigs() {
    return rockAppConfigs;
  }

  public void setRockAppConfigs(List<RockAppConfig> rockAppConfigs) {
    this.rockAppConfigs = rockAppConfigs;
  }

  @Override
  public List<String> getUniqueProperties() {
    return Lists.newArrayList("id");
  }

  @Override
  public List<Object> getUniqueValues() {
    return Lists.newArrayList(id);
  }

  public void addRockAppConfig(RockAppConfig rockAppConfig) {
    rockAppConfigs.add(rockAppConfig);
  }
}
