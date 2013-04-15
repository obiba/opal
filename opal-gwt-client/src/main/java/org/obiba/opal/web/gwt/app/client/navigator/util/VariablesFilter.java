/*******************************************************************************
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.obiba.opal.web.gwt.app.client.navigator.util;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;
import org.obiba.opal.web.model.client.search.VariableItemDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;

/**
 * Helper class to execute filter queries on variables. Will try to filter variables through ElasticSearch
 * if it fails, will query through Magma
 */
public abstract class VariablesFilter extends AbstractVariablesFilter {

  private TableDto table;

  protected String query = "";

  protected Integer limit;

  protected Integer offset;

  protected String sortField = "";

  protected String sortDir = SORT_ASCENDING;

  protected boolean variable = false;

  public VariablesFilter withQuery(String query) {
    this.query = query;
    return this;
  }

  public VariablesFilter withLimit(int limit) {
    this.limit = limit;
    return this;
  }

  public VariablesFilter withOffset(int offset) {
    this.offset = offset;
    return this;
  }

  public VariablesFilter withSortField(String sortField) {
    this.sortField = sortField;
    return this;
  }

  public VariablesFilter withSortDir(String sortDir) {
    this.sortDir = sortDir;
    return this;
  }

  public VariablesFilter withVariable(boolean b) {
    variable = b;
    return this;
  }

  public abstract void beforeVariableResourceCallback();

  public abstract void onVariableResourceCallback(JsArray<VariableDto> variables, boolean isElasticSearch);

  @Override
  public void filter(EventBus eventBus, TableDto table, JsArray<VariableDto> result) {
    this.table = table;

    nextFilter(new VariablesFilterES()).nextFilter(new VariablesFilterMagma());
    next(eventBus, table, result);
  }

  public class VariablesFilterES extends AbstractVariablesFilter {

    @Override
    public void filter(final EventBus eventBus, final TableDto table, final JsArray<VariableDto> results) {

      beforeVariableResourceCallback();

      if(query.isEmpty()) {
        next(eventBus, table, results);
      } else {
        UriBuilder ub = UriBuilder.create()
            .segment("datasource", table.getDatasourceName(), "table", table.getName(), "variables", "_search")
            .query("variable", String.valueOf(variable));

        if(!query.isEmpty()) ub.query("query", query);
        if(limit != null) ub.query("limit", String.valueOf(limit));
        if(!sortField.isEmpty()) ub.query("sortField", sortField);
        if(!sortDir.isEmpty()) ub.query("sortDir", sortDir);

        ResourceRequestBuilderFactory.<QueryResultDto>newBuilder().forResource(ub.build()).get()
            .withCallback(new ResourceCallback<QueryResultDto>() {
              @Override
              public void onResource(Response response, QueryResultDto resultDto) {
                if(response.getStatusCode() == Response.SC_OK) {
                  if(resultDto.getHitsArray() != null && resultDto.getHitsArray().length() > 0) {
                    for(int i = 0; i < resultDto.getHitsArray().length(); i++) {
                      VariableItemDto varDto = (VariableItemDto) resultDto.getHitsArray().get(i)
                          .getExtension(VariableItemDto.ItemResultDtoExtensions.item);

                      results.push(varDto.getVariable());
                    }
                  }
                  onVariableResourceCallback(results, true);
                }
              }
            })//
            .withCallback(Response.SC_BAD_REQUEST, new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                next(eventBus, table, results);
              }
            })//
                // TODO: Not always necessary (on import we should not catch this error here, let Magma handle this)
            .withCallback(Response.SC_SERVICE_UNAVAILABLE, new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                eventBus.fireEvent(NotificationEvent.newBuilder().warn("SearchServiceUnavailable").build());
              }
            }).send();
      }
    }
  }

  public class VariablesFilterMagma extends AbstractVariablesFilter {

    private String cleanFilter(String filter) {
      return filter.replaceAll("/", "\\\\/");
    }

    @Override
    public void filter(EventBus eventBus, TableDto table, final JsArray<VariableDto> result) {
      // Do not use ES
      UriBuilder ub = UriBuilder.create()
          .segment("datasource", table.getDatasourceName(), "table", table.getName(), "variables");//

      if(!query.isEmpty()) ub.query("script", "name().matches(/" + cleanFilter(query) + "/)");
      if(limit != null) ub.query("limit", String.valueOf(limit));
      if(!sortField.isEmpty()) ub.query("sortField", sortField);
      if(!sortDir.isEmpty()) ub.query("sortDir", sortDir);

      ResourceRequestBuilderFactory.<JsArray<VariableDto>>newBuilder().forResource(ub.build()).get()
          .withCallback(new ResourceCallback<JsArray<VariableDto>>() {
            @Override
            public void onResource(Response response, JsArray<VariableDto> resource) {
              if(resource != null && resource.length() > 0) {
                for(int i = 0; i < resource.length(); i++) {
                  result.push(resource.get(i));
                }
              }
              onVariableResourceCallback(result, false);
            }
          }).send();
    }
  }
}
