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
import org.obiba.opal.core.runtime.Service;
import org.obiba.opal.search.es.ElasticSearchConfigurationService;
import org.obiba.opal.search.service.impl.TablesIndexManagerImpl;
import org.obiba.opal.search.service.support.ItemResultDtoStrategy;
import org.obiba.opal.web.model.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;

@Component
public class OpalSearchService implements Service {

  private static final Logger log = LoggerFactory.getLogger(OpalSearchService.class);

  static final String SERVICE_NAME = "search";

  private final ElasticSearchConfigurationService configService;

  private final TablesIndexManager tablesIndexManager;

  private final VariablesIndexManager variablesIndexManager;

  private final ValuesIndexManager valuesIndexManager;

  private final EventBus eventBus;
  private final TablesIndexManagerImpl tablesIndexManagerImpl;

  private boolean running = false;

  @Autowired
  public OpalSearchService(ElasticSearchConfigurationService configService, TablesIndexManager tablesIndexManager, VariablesIndexManager variablesIndexManager, ValuesIndexManager valuesIndexManager, EventBus eventBus, TablesIndexManagerImpl tablesIndexManagerImpl) {
    this.configService = configService;
    this.tablesIndexManager = tablesIndexManager;
    this.variablesIndexManager = variablesIndexManager;
    this.valuesIndexManager = valuesIndexManager;
    this.eventBus = eventBus;
    this.tablesIndexManagerImpl = tablesIndexManagerImpl;
  }

  public boolean isEnabled() {
    return configService.getConfig().isEnabled();
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void start() {
    if (isRunning()) return;
    running = true;
  }

  @Override
  public void stop() {
    running = false;
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
  // Index managers
  //

  public TablesIndexManager getTablesIndexManager() {
    if (!isRunning()) return null;
    return tablesIndexManager;
  }

  public VariablesIndexManager getVariablesIndexManager() {
    if (!isRunning()) return null;
    return variablesIndexManager;
  }

  public ValuesIndexManager getValuesIndexManager() {
    if (!isRunning()) return null;
    return valuesIndexManager;
  }

  //
  // Deprecated operations (related to values indexing),
  // kept for the case it could be partially re-implemented.
  //

  public Collection<String> executeAllIdentifiersQuery(QuerySettings querySettings, String searchPath) throws SearchException {
    if (!isRunning()) return Lists.newArrayList();
    IdentifiersQueryCallback callback = new IdentifiersQueryCallback();
    int from = 0;
    final int size = 1000;
    while (!callback.hasTotal() || callback.getIdentifiers().size() < callback.getTotal()) {
      querySettings.from(from);
      querySettings.size(size);
      // getSearchServicePlugin().executeIdentifiersQuery(querySettings, searchPath, callback);
      from = from + size;
      throw new UnsupportedOperationException("Deprecated");
    }
    return callback.getIdentifiers();
  }

  public void executeIdentifiersQuery(QuerySettings querySettings, String searchPath, IdentifiersQueryCallback callback) throws SearchException {
    if (!isRunning()) return;
    // getSearchServicePlugin().executeIdentifiersQuery(querySettings, searchPath, callback);
    throw new UnsupportedOperationException("Deprecated");
  }

  public Search.EntitiesResultDto.Builder executeEntitiesQuery(QuerySettings querySettings, String searchPath, String entityType, String query) throws SearchException {
    if (!isRunning()) return null;
    // return getSearchServicePlugin().executeEntitiesQuery(querySettings, searchPath, entityType, query);
    throw new UnsupportedOperationException("Deprecated");
  }

  public Search.QueryResultDto executeQuery(QuerySettings querySettings, String searchPath, ItemResultDtoStrategy strategy) throws SearchException {
    if (!isRunning()) return null;
    if (variablesIndexManager.getName().equals(searchPath)) {
      return variablesIndexManager.createQueryExecutor().execute(querySettings);
    } else if (tablesIndexManager.getName().equals(searchPath)) {
      return tablesIndexManager.createQueryExecutor().execute(querySettings);
    }
    // return getSearchServicePlugin().executeQuery(querySettings, searchPath, strategy);
    return Search.QueryResultDto.newBuilder().setTotalHits(0).build();
  }

  public Search.QueryCountDto executeCount(QuerySettings querySettings, String searchPath) throws SearchException {
    if (!isRunning()) return Search.QueryCountDto.newBuilder().setTotalHits(0).build();
    if (variablesIndexManager.getName().equals(searchPath)) {
      return variablesIndexManager.createQueryExecutor().count(querySettings);
    } else if (tablesIndexManager.getName().equals(searchPath)) {
      return tablesIndexManager.createQueryExecutor().count(querySettings);
    }
    // return getSearchServicePlugin().executeQuery(querySettings, searchPath, strategy);
    return Search.QueryCountDto.newBuilder().setTotalHits(0).build();
  }

  //
  // Inner classes
  //

  public static class IdentifiersQueryCallback implements HitsQueryCallback<String> {

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

  //
  // Private methods
  //

  @Subscribe
  public void onValueTableRenamed(ValueTableRenamedEvent event) {
    remove(event.getValueTable());
  }

  @Subscribe
  public void onValueTableUpdated(ValueTableUpdatedEvent event) {
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
    remove(event.getValueTable());
  }

  private void remove(@NotNull ValueTable vt) {
    if (!isRunning()) return;
    // Delete index
    getValuesIndexManager().drop(vt);
    getVariablesIndexManager().drop(vt);
    getTablesIndexManager().drop(vt);

  }

  //
  // Search callbacks
  //

  interface HitsQueryCallback<T> {

    void onTotal(int total);

    void onIdentifier(T id);

  }
}
