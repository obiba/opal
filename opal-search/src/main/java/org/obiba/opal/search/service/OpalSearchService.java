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
import org.obiba.opal.search.IndexSynchronizationManager;
import org.obiba.opal.search.es.ElasticSearchConfiguration;
import org.obiba.opal.search.es.ElasticSearchConfigurationService;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class OpalSearchService implements Service, ElasticSearchProvider {

  public static final String SERVICE_NAME = "search";

  private static final String DEFAULT_SETTINGS_RESOURCE = "org/obiba/opal/search/service/default-settings.yml";

  public static final String PATH_DATA = "${OPAL_HOME}/work/elasticsearch/data";

  public static final String PATH_WORK = "${OPAL_HOME}/work/elasticsearch/work";

  @Autowired
  private ElasticSearchConfigurationService configService;

  @Autowired
  private ApplicationContext applicationContext;

  private Node esNode;

  private Client client;

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
      esNode = NodeBuilder.nodeBuilder() //
          .client(!esConfig.isDataNode()) //
          .settings(ImmutableSettings.settingsBuilder() //
              .classLoader(OpalSearchService.class.getClassLoader()) //
              .loadFromClasspath(DEFAULT_SETTINGS_RESOURCE) //
              .put("path.data", PATH_DATA.replace("${OPAL_HOME}", System.getProperty("OPAL_HOME"))) //
              .put("path.work", PATH_WORK.replace("${OPAL_HOME}", System.getProperty("OPAL_HOME"))) //
              .loadFromSource(esConfig.getEsSettings()) //
          ) //
          .clusterName(esConfig.getClusterName()) //
          .node();
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
      // use applicationContext.getBean() to avoid unresolvable circular reference
      applicationContext.getBean(IndexSynchronizationManager.class).terminateConsumerThread(); // stop indexer thread
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
