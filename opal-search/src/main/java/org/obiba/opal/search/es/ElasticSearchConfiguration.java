/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search.es;

import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.spi.search.SearchSettings;

public class ElasticSearchConfiguration implements OpalConfigurationExtension, SearchSettings {

  private static final String DEFAULT_OPAL_INDEX_NAME = "opal";

  private static final String DEFAULT_CLUSTER_NAME = "opal";

  private Boolean enabled;

  private String clusterName;

  private String indexName;

  private Boolean dataNode;

  private Integer shards;

  private Integer replicas;

  private String esSettings;

  @Override
  public boolean isEnabled() {
    return enabled == null ? true : enabled;
  }

  @Override
  public String getClusterName() {
    return clusterName == null ? DEFAULT_CLUSTER_NAME : clusterName;
  }

  @Override
  public String getIndexName() {
    return indexName == null ? DEFAULT_OPAL_INDEX_NAME : indexName;
  }

  @Override
  public boolean isDataNode() {
    return dataNode == null ? true : dataNode;
  }

  @Override
  public String getEsSettings() {
    return esSettings == null ? "" : esSettings;
  }

  @Override
  public Integer getShards() {
    return shards == null ? 5 : shards;
  }

  @Override
  public Integer getReplicas() {
    return replicas == null ? 1 : replicas;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public void setIndexName(String indexName) {
    this.indexName = indexName;
  }

  public void setDataNode(Boolean dataNode) {
    this.dataNode = dataNode;
  }

  public void setShards(Integer shards) {
    this.shards = shards;
  }

  public void setReplicas(Integer replicas) {
    this.replicas = replicas;
  }

  public void setEsSettings(String esSettings) {
    this.esSettings = esSettings;
  }
}
