package org.obiba.opal.search.service;

import java.io.File;
import java.io.IOException;

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

  private final ElasticSearchConfigurationService configService;

  @Value("${OPAL_HOME}/data")
  private String indexPath;

  private static final Logger log = LoggerFactory.getLogger(SearchServiceConfigurationHandler.class);

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
    if(!config.getClusterName().equals(serviceDto.getExtension(Opal.ESCfgDto.params).getClusterName()) ||
        !config.getIndexName().equals(serviceDto.getExtension(Opal.ESCfgDto.params).getIndexName()) ||
        config.getShards() != serviceDto.getExtension(Opal.ESCfgDto.params).getShards() ||
        config.getReplicas() != serviceDto.getExtension(Opal.ESCfgDto.params).getReplicas()) {
      flushIndices = true;
    }

    config.setClusterName(serviceDto.getExtension(Opal.ESCfgDto.params).getClusterName());
    config.setDataNode(serviceDto.getExtension(Opal.ESCfgDto.params).getDataNode());
    config.setEnabled(serviceDto.getExtension(Opal.ESCfgDto.params).getEnabled());
    config.setEsSettings(serviceDto.getExtension(Opal.ESCfgDto.params).getSettings());
    config.setIndexName(serviceDto.getExtension(Opal.ESCfgDto.params).getIndexName());
    config.setReplicas(serviceDto.getExtension(Opal.ESCfgDto.params).getReplicas());
    config.setShards(serviceDto.getExtension(Opal.ESCfgDto.params).getShards());

    // delete indices and restart service
    if(flushIndices) {
      log.info("Clear Elastic Search indexes: {}", indexPath);
      try {
        File indexDir = new File(indexPath);
        if(indexDir.exists() && indexDir.isDirectory()) {
          deleteDirectoryContents(indexDir);
        } else {
          log.warn("Cannot find Elastic Search indexes: {}", indexPath);
        }
      } catch(IOException e) {
        throw new RuntimeException("Error while clearing Elastic Search indexes " + indexPath, e);
      }
    }
    configService.update(config);
  }

  @Override
  public boolean canGet(OpalConfigurationExtension config) {
    return config instanceof ElasticSearchConfiguration;
  }

  @Override
  public boolean canPut(Opal.ServiceCfgDto serviceDto) {
    return serviceDto.hasExtension(Opal.ESCfgDto.params);
  }

  /**
   * Copied form deprecated com.google.common.io.Files#deleteDirectoryContents(java.io.File) as we don't have symlinks here
   *
   * @param directory
   * @throws IOException
   */
  private static void deleteDirectoryContents(File directory) throws IOException {
    File[] files = directory.listFiles();
    if(files == null) {
      throw new IOException("Error listing files for " + directory);
    }
    for(File file : files) {
      deleteRecursively(file);
    }
  }

  /**
   * Copied form deprecated com.google.common.io.Files#deleteRecursively(java.io.File) as we don't have symlinks here
   *
   * @param directory
   * @throws IOException
   */
  private static void deleteRecursively(File file) throws IOException {
    if(file.isDirectory()) {
      deleteDirectoryContents(file);
    }
    if(!file.delete()) {
      throw new IOException("Failed to delete " + file);
    }
  }
}
