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

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.node.internal.InternalNode;
import org.elasticsearch.rest.RestController;
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.runtime.Service;
import org.obiba.opal.search.IndexSynchronizationManager;
import org.obiba.opal.search.es.ElasticSearchConfiguration;
import org.obiba.opal.search.es.ElasticSearchConfigurationService;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.search.es.support.EsQueryExecutor;
import org.obiba.opal.search.es.support.EsSearchQueryExecutor;
import org.obiba.opal.spi.search.SearchService;
import org.obiba.opal.spi.search.ValuesIndexManager;
import org.obiba.opal.spi.search.VariablesIndexManager;
import org.obiba.opal.web.model.Search;
import org.obiba.opal.web.search.support.SearchQueryExecutor;
import org.obiba.opal.web.search.support.ValueTableIndexManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class OpalSearchService implements Service, ElasticSearchProvider {

  public static final String SERVICE_NAME = "search";

  private static final String DEFAULT_SETTINGS_RESOURCE = "org/obiba/opal/search/service/default-settings.yml";

  public static final String PATH_DATA = "${OPAL_HOME}/work/elasticsearch/data";

  public static final String PATH_WORK = "${OPAL_HOME}/work/elasticsearch/work";

  @Value("${org.obiba.opal.web.search.termsFacetSizeLimit}")
  private int termsFacetSizeLimit;

  @Autowired
  private ElasticSearchConfigurationService configService;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private OpalRuntime opalRuntime;

  @Autowired
  protected ValuesIndexManager valuesIndexManager;

  @Autowired
  protected VariablesIndexManager variablesIndexManager;

  private Node esNode;

  private Client client;

  @Override
  public boolean isEnabled() {
    return configService.getConfig().isEnabled();
  }

  @Override
  public boolean isRunning() {
    return esNode != null || hasSearchServicePlugin() && getSearchServicePlugin().isRunning();
  }

  @Override
  public void start() {
    ElasticSearchConfiguration esConfig = configService.getConfig();
    if (esConfig.isEnabled() && hasSearchServicePlugin() && !getSearchServicePlugin().isRunning()) getSearchServicePlugin().start();

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
  public void stop() {
    if(isRunning()) {
      // use applicationContext.getBean() to avoid unresolvable circular reference
      applicationContext.getBean(IndexSynchronizationManager.class).terminateConsumerThread(); // stop indexer thread
      esNode.close();
      esNode = null;
      client = null;
    }
    if (hasSearchServicePlugin() && getSearchServicePlugin().isRunning()) getSearchServicePlugin().stop();
  }

  @Override
  public String getName() {
    return SERVICE_NAME;
  }

  @Override
  public OpalConfigurationExtension getConfig() throws NoSuchServiceConfigurationException {
    return configService.getConfig();
  }

  //
  // Search methods
  //

  public VariablesIndexManager getVariablesIndexManager() {
    return variablesIndexManager;
  }

  public ValuesIndexManager getValuesIndexManager() {
    return valuesIndexManager;
  }

  public JSONObject executeQuery(JSONObject jsonQuery, String searchPath) throws JSONException {
    EsQueryExecutor queryExecutor = new EsQueryExecutor(this).setSearchPath(searchPath);
    return queryExecutor.executePost(jsonQuery);
  }

  public Search.QueryResultDto executeQuery(String datasource, String table, Search.QueryTermDto queryDto) throws JSONException {
    return createQueryExecutor(datasource, table).execute(queryDto);
  }

  public Search.QueryResultDto executeQuery(String datasource, String table, Search.QueryTermsDto queryDto) throws JSONException {
    return createQueryExecutor(datasource, table).execute(queryDto);
  }

  //
  // Private methods
  //

  private SearchQueryExecutor createQueryExecutor(String datasource, String table) {
    ValueTableIndexManager valueTableIndexManager = new ValueTableIndexManager(getValuesIndexManager(), datasource, table);
    return new EsSearchQueryExecutor(this, valueTableIndexManager, termsFacetSizeLimit);
  }

  private SearchService getSearchServicePlugin() {
    return (SearchService) opalRuntime.getServicePlugin(SearchService.class);
  }

  private boolean hasSearchServicePlugin() {
    return opalRuntime.hasServicePlugins(SearchService.class);
  }

  @Override
  public Client getClient() {
    return client;
  }

  @Override
  public RestController getRest() {
    return ((InternalNode) esNode).injector().getInstance(RestController.class);
  }

}
