package org.obiba.opal.web.magma;

import java.util.List;

import javax.annotation.Nullable;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.annotations.cache.Cache;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableValueSource;

public interface ValueSetsResource {

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
  // Required to allow passing parameters in the body
  @POST
  @Cache(isPrivate = true, mustRevalidate = true, maxAge = 10)
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
