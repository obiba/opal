/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.magma;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.obiba.opal.web.ws.security.AuthorizeResource;

public interface DatasourceTablesResource {


  /**
   * Get the tables of the datasource.
   *
   * @param counts Set the count of entities and of variables (default is true).
   * @param entityType Filter the tables with provided entity type (default is no filter).
   * @param indexedOnly Filter the tables which values have been indexed, index is up to data and search is enabled.
   * @return
   */
  @GET
  List<Magma.TableDto> getTables(@Context Request request, @QueryParam("counts") @DefaultValue("false") boolean counts,
      @Nullable @QueryParam("entityType") String entityType, @QueryParam("indexed") @DefaultValue("false") boolean indexedOnly);

  @GET
  @Path("/excel")
  @Produces("application/vnd.ms-excel")
  @AuthorizeResource
  @AuthenticatedByCookie
  Response getExcelDictionary(@QueryParam("table") List<String> tables) throws MagmaRuntimeException, IOException;

  @POST
  Response createTable(Magma.TableDto table);

  @DELETE
  Response deleteTables(@QueryParam("table") List<String> tables);

  void setDatasource(Datasource datasource);

  List<Magma.TableDto> getTables(boolean counts, String entityType, boolean indexed);
}
