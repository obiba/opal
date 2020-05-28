/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.support;

import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;
import org.obiba.opal.web.model.client.search.VariableItemDto;

import com.google.gwt.core.client.JsArray;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;

/**
 * Helper class to execute filter queries on variables. Will try to filter variables through ElasticSearch
 * if it fails, will query through Magma
 */
public abstract class VariablesFilter extends AbstractVariablesFilter {

  protected String query = "";

  protected Integer limit;

  protected Integer offset;

  protected String sortField = "";

  protected String sortDir = SORT_ASCENDING;

  protected boolean variable = false;

  protected boolean exactMatch = false;

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

  public VariablesFilter isExactMatch(boolean b) {
    exactMatch = b;
    return this;
  }

  public abstract void beforeVariableResourceCallback();

  public abstract void onVariableResourceCallback();

  @Override
  public void filter(EventBus eventBus, TableDto table) {
    this.table = table;

    nextFilter(new VariablesFilterES()).nextFilter(new VariablesFilterMagma());
    next(eventBus, table, results);
  }

  public class VariablesFilterES extends AbstractVariablesFilter {

    @Override
    public void filter(final EventBus eventBus, final TableDto table) {

      beforeVariableResourceCallback();

      if(query.isEmpty() || exactMatch) {
        next(eventBus, table, results);
      } else {
        UriBuilder ub = UriBuilder.create()
            .segment("datasource", table.getDatasourceName(), "table", table.getName(), "variables", "_search")
            .query("variable", String.valueOf(variable));

        if(!query.isEmpty()) ub.query("query", query);
        if(limit != null && limit > 0) ub.query("limit", String.valueOf(limit));
        if(!sortField.isEmpty()) ub.query("sortField", sortField);
        if(!sortDir.isEmpty()) ub.query("sortDir", sortDir);

        ResourceRequestBuilderFactory.<QueryResultDto>newBuilder().forResource(ub.build()).get()
            .withCallback(new ResourceCallback<QueryResultDto>() {
              @Override
              public void onResource(Response response, QueryResultDto resultDto) {
                if(response.getStatusCode() == Response.SC_OK) {
                  for(int i = 0; i < resultDto.getTotalHits(); i++) {
                    VariableItemDto varDto = (VariableItemDto) resultDto.getHitsArray().get(i)
                        .getExtension(VariableItemDto.ItemResultDtoExtensions.item);

                    VariablesFilter.this.results.add(varDto.getVariable());
                  }
                }
                onVariableResourceCallback();
              }
            })//
            .withCallback(new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                // Returns NOT_FOUND when calling with a transient variable, BAD_REQUEST when index is not found
                next(eventBus, table, VariablesFilter.this.results);
              }
            }, Response.SC_BAD_REQUEST, Response.SC_NOT_FOUND, Response.SC_SERVICE_UNAVAILABLE)//
            .send();
      }
    }
  }

  public class VariablesFilterMagma extends AbstractVariablesFilter {

    private String cleanFilter(String filter) {
      return filter.replaceAll("/", "\\\\/").toLowerCase();
    }

    @Override
    public void filter(EventBus eventBus, TableDto table) {
      // Do not use ES
      UriBuilder ub = UriBuilder.create()
          .segment("datasource", table.getDatasourceName(), "table", table.getName(), "variables");//

      if(!query.isEmpty()) ub.query("script", exactMatch
          ? "name().lowerCase().matches(/^" + cleanFilter(query) + "$/)"
          : "name().lowerCase().matches(/" + cleanFilter(query) + "/)");
      if(limit != null && limit > 0) ub.query("limit", String.valueOf(limit));
      if(!sortField.isEmpty()) ub.query("sortField", sortField);
      if(!sortDir.isEmpty()) ub.query("sortDir", sortDir);

      ResourceRequestBuilderFactory.<JsArray<VariableDto>>newBuilder().forResource(ub.build()).get()
          .withCallback(new ResourceCallback<JsArray<VariableDto>>() {
            @Override
            public void onResource(Response response, JsArray<VariableDto> resource) {
              if(resource != null && resource.length() > 0) {
                for(int i = 0; i < resource.length(); i++) {
                  VariablesFilter.this.results.add(resource.get(i));
                }
              }
              onVariableResourceCallback();
            }
          })//
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              // ignore
            }
          }, Response.SC_NOT_FOUND)//
          .send();
    }
  }
}
