/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.obiba.opal.core.domain.HasUniqueProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * A high-level abstraction of an external application offering some type of service which has its own lifecycle. Application instances
 * can be registered directly to Opal or discovered though Consul.
 */
public class App implements HasUniqueProperties {

  private String id;

  private String name;

  private String cluster;

  private String type;

  private String server;

  private List<String> tags = Lists.newArrayList();

  public App() {
  }

  public App(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get unique app name.
   *
   * @return
   */
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the type.
   *
   * @return
   */
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  /**
   * Server running the app instance.
   *
   * @return
   */
  public String getServer() {
    return server;
  }

  public void setServer(String server) {
    this.server = server;
  }

  public boolean hasServer() {
    return !Strings.isNullOrEmpty(server);
  }

  public String getCluster() {
    return cluster;
  }

  public void setCluster(String cluster) {
    this.cluster = cluster;
  }

  public boolean hasCluster() {
    return !Strings.isNullOrEmpty(cluster);
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
    result = ((result * 31) + ((this.type == null) ? 0 : this.type.hashCode()));
    result = ((result * 31) + ((this.cluster == null) ? 0 : this.cluster.hashCode()));
    result = ((result * 31) + ((this.server == null) ? 0 : this.server.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (!(other instanceof App)) {
      return false;
    }
    App rhs = ((App) other);
    return this.name.equals(rhs.name)
        && this.type.equals(rhs.type)
        && this.cluster.equals(rhs.cluster)
        && this.server.equals(rhs.server);
  }

  @Override
  public List<String> getUniqueProperties() {
    return Lists.newArrayList("id");
  }

  @Override
  public List<Object> getUniqueValues() {
    return Lists.newArrayList(id);
  }

  @Override
  public String toString() {
    if (id == null)
      return "App{" +
          "name='" + name + '\'' +
          ", type='" + type + '\'' +
          ", cluster='" + cluster + '\'' +
          ", server='" + server + '\'' +
          '}';
    else
      return "App{" +
          "id='" + id + '\'' +
          ", name='" + name + '\'' +
          ", type='" + type + '\'' +
          ", cluster='" + cluster + '\'' +
          ", server='" + server + '\'' +
          '}';
  }
}
