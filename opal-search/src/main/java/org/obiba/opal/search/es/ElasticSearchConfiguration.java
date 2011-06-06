/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
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

  private String clusterName;

  private String indexName;

  private Boolean dataNode;

  private Integer shards;

  private Integer replicas;

  private String esSettings;

  public String getClusterName(String defaultName) {
    return clusterName != null ? clusterName : defaultName;
  }

  public String getIndexName(String defaultName) {
    return indexName != null ? indexName : defaultName;
  }

  public boolean isDataNode() {
    return dataNode != null ? dataNode : Boolean.TRUE;
  }

  public String getEsSettings() {
    return esSettings != null ? esSettings : "";
  }

  public Integer getShards() {
    return shards != null ? shards : 5;
  }

  public Integer getReplicas() {
    return replicas != null ? replicas : 1;
  }

}
