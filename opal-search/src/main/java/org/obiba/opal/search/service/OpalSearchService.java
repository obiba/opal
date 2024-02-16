/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search.service;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.obiba.magma.ValueTable;
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.event.*;
import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.runtime.Service;
import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.obiba.opal.search.es.ElasticSearchConfiguration;
import org.obiba.opal.search.es.ElasticSearchConfigurationService;
import org.obiba.opal.search.event.SynchronizeIndexEvent;
import org.obiba.opal.spi.search.*;
import org.obiba.opal.spi.search.support.ItemResultDtoStrategy;
import org.obiba.opal.web.model.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadFactory;

@Component
public class OpalSearchService implements Service {

  private static final Logger log = LoggerFactory.getLogger(OpalSearchService.class);

  static final String SERVICE_NAME = "search";

  private final ElasticSearchConfigurationService configService;

  private final OpalGeneralConfigService opalGeneralConfigService;

  private final VariableSummaryHandler variableSummaryHandler;

  private final ThreadFactory threadFactory;

  private final EventBus eventBus;

  private OpalRuntime opalRuntime;

  @Autowired
  public OpalSearchService(ElasticSearchConfigurationService configService, OpalGeneralConfigService opalGeneralConfigService, VariableSummaryHandler variableSummaryHandler, ThreadFactory threadFactory, EventBus eventBus) {
    this.configService = configService;
    this.opalGeneralConfigService = opalGeneralConfigService;
    this.variableSummaryHandler = variableSummaryHandler;
    this.threadFactory = threadFactory;
    this.eventBus = eventBus;
  }

  public boolean isEnabled() {
    return configService.getConfig().isEnabled();
  }

  @Override
  public void initialize(OpalRuntime opalRuntime) {
    this.opalRuntime = opalRuntime;
  }

  @Override
  public boolean isRunning() {
    return hasSearchServicePlugin() && getSearchServicePlugin().isRunning();
  }

  @Override
  public void start() {
    if (isRunning()) return;
    SearchSettings esConfig = new OpalSearchSettings(configService.getConfig());
    if (!esConfig.isEnabled()) return;
    if (!hasSearchServicePlugin()) {
      log.warn("No Search Service plugin found.");
      return;
    }
    SearchService service = getSearchServicePlugin();
    service.configure(esConfig, variableSummaryHandler, threadFactory);
    service.start();
  }

  @Override
  public void stop() {
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
    if (!isRunning()) return null;
    return getSearchServicePlugin().getVariablesIndexManager();
  }

  public ValuesIndexManager getValuesIndexManager() {
    if (!isRunning()) return null;
    return getSearchServicePlugin().getValuesIndexManager();
  }

  public Collection<String> executeAllIdentifiersQuery(QuerySettings querySettings, String searchPath) throws SearchException {
    if (!isRunning()) return Lists.newArrayList();
    IdentifiersQueryCallback callback = new IdentifiersQueryCallback();
    int from = 0;
    final int size = 1000;
    while (!callback.hasTotal() || callback.getIdentifiers().size() < callback.getTotal()) {
      querySettings.from(from);
      querySettings.size(size);
      getSearchServicePlugin().executeIdentifiersQuery(querySettings, searchPath, callback);
      from = from + size;
    }
    return callback.getIdentifiers();
  }

  public void executeIdentifiersQuery(QuerySettings querySettings, String searchPath, IdentifiersQueryCallback callback) throws SearchException {
    if (!isRunning()) return;
    getSearchServicePlugin().executeIdentifiersQuery(querySettings, searchPath, callback);
  }

  public Search.EntitiesResultDto.Builder executeEntitiesQuery(QuerySettings querySettings, String searchPath, String entityType, String query) throws SearchException {
    if (!isRunning()) return null;
    return getSearchServicePlugin().executeEntitiesQuery(querySettings, searchPath, entityType, query);
  }

  public Search.QueryResultDto executeQuery(QuerySettings querySettings, String searchPath, ItemResultDtoStrategy strategy) throws SearchException {
    if (!isRunning()) return null;
    return getSearchServicePlugin().executeQuery(querySettings, searchPath, strategy);
  }

  public Search.QueryResultDto executeQuery(String datasource, String table, Search.QueryTermDto queryDto) throws SearchException {
    if (!isRunning()) return null;
    return getSearchServicePlugin().executeQuery(datasource, table, queryDto);
  }

  public Search.QueryResultDto executeQuery(String datasource, String table, Search.QueryTermsDto queryDto) throws SearchException {
    if (!isRunning()) return null;
    return getSearchServicePlugin().executeQuery(datasource, table, queryDto);
  }

  public static class IdentifiersQueryCallback implements SearchService.HitsQueryCallback<String> {

    private int total = -1;

    private List<String> identifiers = Lists.newArrayList();

    @Override
    public void onTotal(int total) {
      this.total = total;
    }

    @Override
    public void onIdentifier(String id) {
      identifiers.add(id);
    }

    public boolean hasTotal() {
      return total > -1;
    }

    public int getTotal() {
      return total;
    }
    
    public List<String> getIdentifiers() {
      return identifiers;
    }
  }

  public class OpalSearchSettings implements SearchSettings {

    private final ElasticSearchConfiguration elasticSearchConfiguration;

    public OpalSearchSettings(ElasticSearchConfiguration elasticSearchConfiguration) {
      this.elasticSearchConfiguration = elasticSearchConfiguration;
    }

    @Override
    public String getClusterName() {
      return elasticSearchConfiguration.getClusterName();
    }

    @Override
    public String getIndexName() {
      return elasticSearchConfiguration.getIndexName();
    }

    @Override
    public boolean isDataNode() {
      return elasticSearchConfiguration.isDataNode();
    }

    @Override
    public String getEsSettings() {
      return elasticSearchConfiguration.getEsSettings();
    }

    @Override
    public boolean isEnabled() {
      return elasticSearchConfiguration.isEnabled();
    }

    @Override
    public Integer getShards() {
      return elasticSearchConfiguration.getShards();
    }

    @Override
    public Integer getReplicas() {
      return elasticSearchConfiguration.getReplicas();
    }

    @Override
    public List<String> getLocales() {
      return opalGeneralConfigService.getConfig().getLocalesAsString();
    }
  }

  //
  // Private methods
  //

  private SearchService getSearchServicePlugin() {
    return (SearchService) opalRuntime.getServicePlugin(SearchService.class);
  }

  private boolean hasSearchServicePlugin() {
    return opalRuntime.hasServicePlugins(SearchService.class);
  }

  @Subscribe
  public void onValueTableRenamed(ValueTableRenamedEvent event) {
    remove(event.getValueTable());
  }

  @Subscribe
  public void onValueTableDeleted(ValueTableDeletedEvent event) {
    remove(event.getValueTable());
  }

  @Subscribe
  public void onVariableRenamed(VariableRenamedEvent event) {
    remove(event.getValueTable());
  }

  @Subscribe
  public void onVariableDeleted(VariableDeletedEvent event) {
    remove(event.getValueTable());
  }

  @Subscribe
  public void onVariablesUpdated(VariablesUpdatedEvent event) {
    if (!isRunning()) return;
    // to ensure variable search is correct
    getVariablesIndexManager().getIndex(event.getValueTable()).delete();
    // synchronize variable index
    eventBus.post(new SynchronizeIndexEvent(getVariablesIndexManager(), event.getValueTable()));
  }

  private void remove(@NotNull ValueTable vt) {
    if (!isRunning()) return;
    // Delete index
    getValuesIndexManager().getIndex(vt).delete();
    getVariablesIndexManager().getIndex(vt).delete();
  }

}
