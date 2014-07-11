package org.obiba.opal.web.magma;

import java.util.Locale;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.annotations.cache.Cache;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;

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
  @Cache(isPrivate = true, mustRevalidate = true, maxAge = 0)
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
  @Cache(isPrivate = true, mustRevalidate = true, maxAge = 0)
  Response getValue(@QueryParam("pos") Integer pos);
}
