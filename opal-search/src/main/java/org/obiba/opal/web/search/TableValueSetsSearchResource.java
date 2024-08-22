/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.search;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.opal.search.AbstractSearchUtility;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.search.service.SearchException;
import org.obiba.opal.search.service.ValuesIndexManager;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Search;
import org.obiba.opal.web.search.support.RQLParserFactory;
import org.obiba.opal.web.search.support.VariableEntityValueSetDtoFunction;
import org.obiba.opal.web.ws.SortDir;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("request")
@Path("/datasource/{ds}/table/{table}/valueSets/_search")
public class TableValueSetsSearchResource extends AbstractSearchUtility {

//  private static final Logger log = LoggerFactory.getLogger(TableVariablesSearchResource.class);

  @PathParam("ds")
  private String datasource;

  @PathParam("table")
  private String table;

  @GET
  @Transactional(readOnly = true)
  @SuppressWarnings("PMD.ExcessiveParameterList")
  public Response search(@Context UriInfo uriInfo, @QueryParam("ql") @DefaultValue("rql") String queryLanguage, @QueryParam("query") String query,
                         @QueryParam("offset") @DefaultValue("0") int offset, @QueryParam("limit") @DefaultValue("10") int limit,
                         @QueryParam("select") String select) throws SearchException {

    String esQuery = query;

    if (!"es".equals(queryLanguage))
      esQuery = RQLParserFactory.parse(query, opalSearchService.getValuesIndexManager());

    if (!canQueryEsIndex()) return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    if (!opalSearchService.getValuesIndexManager().hasIndex(getValueTable()))
      return Response.status(Response.Status.NOT_FOUND).build();

    esQuery = "reference:\"" + getValueTable().getTableReference() + (Strings.isNullOrEmpty(esQuery) ? "\"" : "\" AND " + esQuery);
    OpalSearchService.IdentifiersQueryCallback callback = new OpalSearchService.IdentifiersQueryCallback();
    opalSearchService.executeIdentifiersQuery(buildQuerySearch(esQuery, offset, limit,
        Lists.newArrayList("identifier"), null, "identifier", SortDir.ASC.name()).noDefaultFields(), getSearchPath(), callback);
    Search.ValueSetsResultDto.Builder dtoResponseBuilder = getValueSetsDtoBuilder(uriInfo, select, callback.getTotal(), callback.getIdentifiers());
    return Response.ok().entity(dtoResponseBuilder.build()).build();
  }

  private Search.ValueSetsResultDto.Builder getValueSetsDtoBuilder(UriInfo uriInfo, String select,
                                                                   int totalIds, List<String> ids) throws SearchException {
    Search.ValueSetsResultDto.Builder dtoResponseBuilder = Search.ValueSetsResultDto.newBuilder();
    dtoResponseBuilder.setTotalHits(totalIds);
    String entityType = getValueTable().getEntityType();
    Collection<VariableEntity> entities = ids.stream().map(id -> new VariableEntityBean(entityType, id)).collect(Collectors.toList());
    String path = uriInfo.getPath();
    path = path.substring(0, path.indexOf("/_search"));
    dtoResponseBuilder.setValueSets(getValueSetsDto(path, select, entities));
    return dtoResponseBuilder;
  }

  //
  // Protected methods
  //

  @Override
  protected String getSearchPath() {
    ValuesIndexManager manager = opalSearchService.getValuesIndexManager();
    return manager.getName() + "/" + manager.getIndex(getValueTable()).getIndexType();
  }

  //
  // Private methods
  //

  private boolean canQueryEsIndex() {
    return searchServiceAvailable() && opalSearchService.getValuesIndexManager().isReady() &&
        opalSearchService.getValuesIndexManager().isIndexUpToDate(getValueTable());
  }

  private ValueTable getValueTable() {
    return MagmaEngine.get().getDatasource(datasource).getValueTable(table);
  }

  private Magma.ValueSetsDto getValueSetsDto(String uriInfoPath, String select,
                                             Iterable<VariableEntity> variableEntities) {
    Iterable<Variable> variables = filterVariables(select);

    Magma.ValueSetsDto.Builder builder = Magma.ValueSetsDto.newBuilder().setEntityType(getValueTable().getEntityType());

    builder.addAllVariables(Iterables.transform(variables, new Function<Variable, String>() {

      @Override
      public String apply(Variable from) {
        return from.getName();
      }

    }));

    ImmutableList.Builder<Magma.ValueSetsDto.ValueSetDto> valueSetDtoBuilder = ImmutableList.builder();
    Iterable<Magma.ValueSetsDto.ValueSetDto> transform = Iterables.transform(getValueTable().getValueSets(variableEntities),
        new VariableEntityValueSetDtoFunction(getValueTable(), variables, uriInfoPath, true));

    for (Magma.ValueSetsDto.ValueSetDto valueSetDto : transform) {
      valueSetDtoBuilder.add(valueSetDto);
    }

    builder.addAllValueSets(valueSetDtoBuilder.build());

    return builder.build();
  }

  protected Iterable<Variable> filterVariables(String script) {
    List<Variable> filteredVariables;

    if (StringUtils.isEmpty(script)) {
      filteredVariables = Lists.newArrayList(getValueTable().getVariables());
    } else {
      JavascriptClause jsClause = new JavascriptClause(script);
      jsClause.initialise();

      filteredVariables = new ArrayList<>();
      for (Variable variable : getValueTable().getVariables()) {
        if (jsClause.select(variable)) {
          filteredVariables.add(variable);
        }
      }
    }

    return filteredVariables;
  }

}
