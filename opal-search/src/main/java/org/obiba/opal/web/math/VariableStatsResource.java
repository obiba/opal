/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.math;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;

import org.jboss.resteasy.annotations.cache.Cache;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.core.domain.VariableNature;
import org.obiba.opal.search.StatsIndexManager;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.web.TimestampedResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
@Scope("request")
@Path("/datasource/{ds}/table/{table}/variable/{variable}/stats")
public class VariableStatsResource {

  @Autowired
  private OpalSearchService opalSearchService;

  @Autowired
  private StatsIndexManager statsIndexManager;

  @Autowired
  private ElasticSearchProvider esProvider;

  @PathParam("ds")
  private String datasourceName;

  @PathParam("table")
  private String tableName;

  @PathParam("variable")
  private String variableName;

  // TODO _transient/stats/summary

  @Path("/summary")
  @Cache(isPrivate = true, mustRevalidate = true, maxAge = 0)
  public AbstractSummaryResource getSummary(@Context Request request, @QueryParam("nature") String natureStr) {

    TimestampedResponses.evaluate(request, getValueTable());

    Variable variable = getVariable();
    VariableNature nature = natureStr == null
        ? VariableNature.getNature(variable)
        : VariableNature.valueOf(natureStr.toUpperCase());

    switch(nature) {
      case CATEGORICAL:
        return new CategoricalSummaryResource(opalSearchService, statsIndexManager, esProvider, getValueTable(),
            variable);
      case CONTINUOUS:
        return new ContinuousSummaryResource(opalSearchService, statsIndexManager, esProvider, getValueTable(),
            variable, null);
      case TEMPORAL:
      case UNDETERMINED:
      default:
        return new DefaultSummaryResource(opalSearchService, statsIndexManager, esProvider, getValueTable(), variable,
            null);
    }
  }

  private Datasource getDatasource() {
    return MagmaEngine.get().getDatasource(datasourceName);
  }

  private ValueTable getValueTable() {
    return getDatasource().getValueTable(tableName);
  }

  private Variable getVariable() {
    return getValueTable().getVariable(variableName);
  }
}