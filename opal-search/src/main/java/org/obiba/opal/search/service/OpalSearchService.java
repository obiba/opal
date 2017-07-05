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
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.runtime.Service;
import org.obiba.opal.search.es.ElasticSearchConfigurationService;
import org.obiba.opal.spi.search.*;
import org.obiba.opal.web.model.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadFactory;

@Component
public class OpalSearchService implements Service {

  private static final Logger log = LoggerFactory.getLogger(OpalSearchService.class);

  static final String SERVICE_NAME = "search";

  @Autowired
  private ElasticSearchConfigurationService configService;

  @Autowired
  private OpalRuntime opalRuntime;

  @Autowired
  private VariableSummaryHandler variableSummaryHandler;

  @Autowired
  private ThreadFactory threadFactory;

  public boolean isEnabled() {
    return configService.getConfig().isEnabled();
  }

  @Override
  public boolean isRunning() {
    return hasSearchServicePlugin() && getSearchServicePlugin().isRunning();
  }

  @Override
  public void start() {
    if (isRunning()) return;
    SearchSettings esConfig = configService.getConfig();
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

  public JSONObject executeQuery(JSONObject jsonQuery, String searchPath) throws JSONException {
    if (!isRunning()) return null;
    return getSearchServicePlugin().executeQuery(jsonQuery, searchPath);
  }

  public Search.QueryResultDto executeQuery(String datasource, String table, Search.QueryTermDto queryDto) throws JSONException {
    if (!isRunning()) return null;
    return getSearchServicePlugin().executeQuery(datasource, table, queryDto);
  }

  public Search.QueryResultDto executeQuery(String datasource, String table, Search.QueryTermsDto queryDto) throws JSONException {
    if (!isRunning()) return null;
    return getSearchServicePlugin().executeQuery(datasource, table, queryDto);
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

}
