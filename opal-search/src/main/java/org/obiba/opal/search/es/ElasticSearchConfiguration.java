/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.search.es;

import org.obiba.opal.core.cfg.OpalConfigurationExtension;

public class ElasticSearchConfiguration implements OpalConfigurationExtension {

  private Boolean enabled;

  private String clusterName;

  private String indexName;

  private Boolean dataNode;

  private Integer shards;

  private Integer replicas;

  private String esSettings;

  public boolean isEnabled() {
    return enabled == null ? true : enabled;
  }

  public String getClusterName() {
    return clusterName == null ? EsIndexManager.DEFAULT_CLUSTER_NAME : clusterName;
  }

  public String getIndexName() {
    return indexName == null ? EsIndexManager.DEFAULT_OPAL_INDEX_NAME : indexName;
  }

  public boolean isDataNode() {
    return dataNode == null ? true : dataNode;
  }

  public String getEsSettings() {
    return esSettings == null ? "" : esSettings;
  }

  public Integer getShards() {
    return shards == null ? 5 : shards;
  }

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
