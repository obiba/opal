/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.obiba.opal.web.search;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.core.domain.VariableNature;
import org.obiba.opal.search.IndexManager;
import org.obiba.opal.search.ValueTableIndex;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.web.model.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/datasource/{ds}/table/{table}/facet")

public class FacetIndexResource {

    private static final Logger log = LoggerFactory.getLogger(FacetIndexResource.class);

    private final ElasticSearchProvider esProvider;

    private final IndexManager indexManager;

    @PathParam("ds")
    private String datasource;

    @PathParam("table")
    private String table;

    @Autowired
    public FacetIndexResource(IndexManager indexManager, ElasticSearchProvider esProvider) {
        this.indexManager = indexManager;
        this.esProvider = esProvider;
    }

    @POST
    @Path("/variable/{variable}/_search")
    public Response search(@Context HttpServletRequest servletRequest, @PathParam("variable") String variable) {
        log.warn("Searching facette for variable " + variable);

        Variable var = getValueTable().getVariable(variable);
        VariableNature nature = VariableNature.getNature(var);
        Search.QueryResultDto dtoResult = Search.QueryResultDto.newBuilder().build();

        try {
            QueryTermDtoBuilder dtoBuilder = new QueryTermDtoBuilder("0");

            switch(nature) {
                case CATEGORICAL:
                    dtoBuilder.categoricalVariableTermDto(variable);
                    break;

                case CONTINUOUS:
                    dtoBuilder.continuousVariableTermDto(variable);
                    break;

                case TEMPORAL: // fall through
                case UNDETERMINED:
                    log.warn(variable + " not processed");
                    return Response.ok().build();
            }

            ElasticSearchQuery esQuery = new ElasticSearchQuery(servletRequest, esProvider);
            dtoResult = esQuery.execute(getValueTableIndex(), dtoBuilder.build());
            log.info("Query result " + dtoResult);

        } catch (Exception e) {
            log.warn("Error " + e.getMessage());
        }

        return Response.ok().entity(dtoResult).build();
    }

    private ValueTableIndex getValueTableIndex() {
        return this.indexManager.getIndex(getValueTable());
    }

    private ValueTable getValueTable() {
        log.warn("Getting the value table");
        return MagmaEngine.get().getDatasource(datasource).getValueTable(table);
    }

}
