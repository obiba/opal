/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.magma;

import java.util.List;

import jakarta.annotation.Nullable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableValueSource;
import org.obiba.opal.web.BaseResource;

public interface ValueSetsResource extends BaseResource {

  void setValueTable(ValueTable valueTable);

  void setVariableValueSource(@Nullable VariableValueSource variableValueSource);

  /**
   * Get a chunk of value sets, optionally filters the variables and/or the entities.
   *
   * @param select script for filtering the variables
   * @param offset
   * @param limit
   * @return
   */
  @GET
  Response getValueSets(@Context UriInfo uriInfo, //
      @QueryParam("select") String select, //
      @QueryParam("offset") @DefaultValue("0") int offset, //
      @QueryParam("limit") @DefaultValue("100") int limit, //
      @QueryParam("filterBinary") @DefaultValue("true") Boolean filterBinary);

  /**
   * Remove all value sets of the table.
   *
   * @param identifiers
   * @return
   */
  @DELETE
  Response drop(@QueryParam("id") List<String> identifiers);

  /**
   * Get the value set timestamps without the values.
   *
   * @param offset
   * @param limit
   * @return
   */
  @GET
  @Path("/timestamps")
  Response getValueSetsTimestamps(@QueryParam("offset") @DefaultValue("0") int offset, //
      @QueryParam("limit") @DefaultValue("100") int limit);
}
