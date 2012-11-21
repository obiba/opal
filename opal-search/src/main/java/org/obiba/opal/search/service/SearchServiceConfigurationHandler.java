package org.obiba.opal.search.service;

import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.search.es.ElasticSearchConfiguration;
import org.obiba.opal.search.es.ElasticSearchConfigurationService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.services.ServiceConfigurationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SearchServiceConfigurationHandler implements ServiceConfigurationHandler {

  private final ElasticSearchConfigurationService configService;

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

    config.setClusterName(serviceDto.getExtension(Opal.ESCfgDto.params).getClusterName());
    config.setDataNode(serviceDto.getExtension(Opal.ESCfgDto.params).getDataNode());
    config.setEnabled(serviceDto.getExtension(Opal.ESCfgDto.params).getEnabled());
    config.setEsSettings(serviceDto.getExtension(Opal.ESCfgDto.params).getSettings());
    config.setIndexName(serviceDto.getExtension(Opal.ESCfgDto.params).getIndexName());
    config.setReplicas(serviceDto.getExtension(Opal.ESCfgDto.params).getReplicas());
    config.setShards(serviceDto.getExtension(Opal.ESCfgDto.params).getShards());

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
}
