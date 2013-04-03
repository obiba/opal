/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search.service;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.node.internal.InternalNode;
import org.elasticsearch.rest.RestController;
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.core.runtime.Service;
import org.obiba.opal.search.es.ElasticSearchConfiguration;
import org.obiba.opal.search.es.ElasticSearchConfigurationService;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OpalSearchService implements Service, ElasticSearchProvider {

  public static final String SERVICE_NAME = "search";

  private static final String DEFAULT_SETTINGS_RESOURCE = "org/obiba/opal/search/service/default-settings.yml";

  private final ElasticSearchConfigurationService configService;

  private Node esNode;

  private Client client;

  @Autowired
  public OpalSearchService(ElasticSearchConfigurationService configService) {
    if(configService == null) throw new IllegalArgumentException("configService cannot be null");
    this.configService = configService;
  }

  @Override
  public boolean isEnabled() {
    return configService.getConfig().isEnabled();
  }

  @Override
  public boolean isRunning() {
    return esNode != null;
  }

  @Override
  public void start() {
    ElasticSearchConfiguration esConfig = configService.getConfig();
    if(!isRunning() && esConfig.isEnabled()) {
      esNode = NodeBuilder.nodeBuilder().client(true) //
          .settings(ImmutableSettings.settingsBuilder() //
              .put("http.enabled", false) //
              .put("discovery.zen.ping.multicast.enabled", false) //
              .classLoader(OpalSearchService.class.getClassLoader())
              .loadFromClasspath(DEFAULT_SETTINGS_RESOURCE) //
              .loadFromSource(esConfig.getEsSettings())) //
          .clusterName(esConfig.getClusterName()) //
          .client(!esConfig.isDataNode()).node();
      client = esNode.client();
    }
  }

  @Override
  public Client getClient() {
    return client;
  }

  @Override
  public RestController getRest() {
    return ((InternalNode) esNode).injector().getInstance(RestController.class);
  }

  @Override
  public void stop() {
    if(isRunning()) {
      esNode.close();
      esNode = null;
      client = null;
    }
  }

  @Override
  public String getName() {
    return SERVICE_NAME;
  }

  @Override
  public OpalConfigurationExtension getConfig() throws NoSuchServiceConfigurationException {
    return configService.getConfig();
  }
}
