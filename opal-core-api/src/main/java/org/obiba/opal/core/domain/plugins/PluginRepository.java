/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.domain.plugins;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

/**
 * Plugin repository description.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PluginRepository {
  private final Date updated;
  private final List<PluginPackage> plugins;

  public PluginRepository(@JsonProperty("updated") Date updated, @JsonProperty("plugins") List<PluginPackage> plugins) {
    this.updated = updated;
    this.plugins = plugins;
  }

  public List<PluginPackage> getPlugins() {
    return plugins;
  }
}
