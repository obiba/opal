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

import jakarta.annotation.Nullable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;

import javax.validation.constraints.NotNull;
import java.util.Locale;
import java.util.Set;

public interface ValueSetResource {

  void setEntity(@NotNull VariableEntity entity);

  void setVariableValueSource(@Nullable VariableValueSource vvs);

  void setLocales(Set<Locale> locales);

  void setValueTable(ValueTable valueTable);

  /**
   * Get a chunk of value sets, optionally filters the variables and/or the entities.
   *
   * @param select script for filtering the variables
   * @return
   */
  @GET
  Response getValueSet(@Context UriInfo uriInfo, @QueryParam("select") String select,
      @QueryParam("filterBinary") @DefaultValue("true") Boolean filterBinary);

  /**
   * Remove this value set from its table.
   *
   * @return
   */
  @DELETE
  Response drop();

  /**
   * Get a value, optionally providing the position (start at 0) of the value in the case of a value sequence.
   */
  @GET
  @Path("/value")
  Response getValue(@QueryParam("pos") Integer pos);
}
