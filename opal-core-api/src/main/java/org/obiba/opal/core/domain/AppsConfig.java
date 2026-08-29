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
import jakarta.persistence.*;
import org.obiba.opal.core.domain.converter.RockAppConfigListConverter;

import java.util.List;

/**
 * Also a single row: the id is fixed at "1" and has been since it was written, which is what makes it the key.
 */
@Entity
@Table(name = "apps_config")
public class AppsConfig  {

  @Id
  private String id = "1";

  private String token;

  @Lob
  @Convert(converter = RockAppConfigListConverter.class)
  @Column(name = "rock_app_configs")
  private List<RockAppConfig> rockAppConfigs = Lists.newArrayList();

  public String getId() {
    return id;
  }

  // Self registration

  public void setToken(String token) {
    this.token = token;
  }

  public String getToken() {
    return token;
  }

  // Rock apps

  public List<RockAppConfig> getRockAppConfigs() {
    return rockAppConfigs;
  }

  public void setRockAppConfigs(List<RockAppConfig> rockAppConfigs) {
    this.rockAppConfigs = rockAppConfigs;
  }

  public void addRockAppConfig(RockAppConfig rockAppConfig) {
    rockAppConfigs.add(rockAppConfig);
  }

  // DB methods

}
