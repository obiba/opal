/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.search.service;

import java.io.File;
import java.io.IOException;

import org.obiba.core.util.FileUtil;
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.search.es.ElasticSearchConfiguration;
import org.obiba.opal.search.es.ElasticSearchConfigurationService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.services.ServiceConfigurationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SearchServiceConfigurationHandler implements ServiceConfigurationHandler {

  private static final Logger log = LoggerFactory.getLogger(SearchServiceConfigurationHandler.class);

  private final ElasticSearchConfigurationService configService;

  @Value("${OPAL_HOME}/work/elastic-search")
  private String indexPath;

  @Autowired
  public SearchServiceConfigurationHandler(ElasticSearchConfigurationService configService) {
    this.configService = configService;
  }

  @Override
  public Opal.ServiceCfgDto get(OpalConfigurationExtension opalConfig) {
    ElasticSearchConfiguration config = (ElasticSearchConfiguration) opalConfig;

    Opal.ESCfgDto escDto = Opal.ESCfgDto.newBuilder().setClusterName(config.getClusterName())
        .setIndexName(config.getIndexName()).setDataNode(config.isDataNode()).setShards(config.getShards())
        .setEnabled(config.isEnabled()).setReplicas(config.getReplicas()).setSettings(config.getEsSettings()).build();

    return Opal.ServiceCfgDto.newBuilder().setName(OpalSearchService.SERVICE_NAME)
        .setExtension(Opal.ESCfgDto.params, escDto).build();
  }

  @Override
  public void put(Opal.ServiceCfgDto serviceDto) {
    ElasticSearchConfiguration config = configService.getConfig();

    boolean flushIndices = false;
    // When changing cluster name, index name, shards number or replicas number
    Opal.ESCfgDto esCfgDto = serviceDto.getExtension(Opal.ESCfgDto.params);
    if(!config.getClusterName().equals(esCfgDto.getClusterName()) ||
        !config.getIndexName().equals(esCfgDto.getIndexName()) ||
        config.getShards() != esCfgDto.getShards() ||
        config.getReplicas() != esCfgDto.getReplicas()) {
      flushIndices = true;
    }

    config.setClusterName(esCfgDto.getClusterName());
    config.setDataNode(esCfgDto.getDataNode());
    config.setEnabled(esCfgDto.getEnabled());
    config.setEsSettings(esCfgDto.getSettings());
    config.setIndexName(esCfgDto.getIndexName());
    config.setReplicas(esCfgDto.getReplicas());
    config.setShards(esCfgDto.getShards());

    // delete indices and restart service
    if(flushIndices) {
      deleteESData();
    }
    configService.update(config);
  }

  private void deleteESData() {
    log.info("Clear Elastic Search indexes: {}", indexPath);
    try {
      File indexDir = new File(indexPath);
      if(!FileUtil.delete(indexDir)) {
        log.warn("Cannot find Elastic Search indexes: {}", indexPath);
      }
    } catch(IOException e) {
      throw new RuntimeException("Error while clearing Elastic Search indexes " + indexPath, e);
    }
  }

  @Override
  public boolean canGet(OpalConfigurationExtension config) {
    return config instanceof ElasticSearchConfiguration;
  }

  @Override
  public boolean canPut(Opal.ServiceCfgDto serviceDto) {
    return serviceDto.hasExtension(Opal.ESCfgDto.params);
  }

}
