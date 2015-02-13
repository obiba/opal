package org.obiba.opal.web.magma;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
import javax.ws.rs.core.UriInfo;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.obiba.opal.web.ws.security.AuthorizeResource;

import edu.umd.cs.findbugs.annotations.Nullable;

public interface VariablesResource {

  void setValueTable(ValueTable valueTable);

  void setLocales(Set<Locale> locales);

  /**
   * Get a chunk of variables, optionally filtered by a script
   *
   * @param uriInfo
   * @param script script for filtering the variables
   * @param offset
   * @param limit
   * @return
   */
  @GET
  Iterable<Magma.VariableDto> getVariables(@Context Request request, @Context UriInfo uriInfo, @QueryParam("script") String script,
      @QueryParam("offset") @DefaultValue("0") Integer offset, @Nullable @QueryParam("limit") Integer limit);

  @GET
  @Path("/excel")
  @Produces("application/vnd.ms-excel")
  @AuthenticatedByCookie
  @AuthorizeResource
  Response getExcelDictionary(@Context Request request) throws MagmaRuntimeException, IOException;

  @POST
  Response addOrUpdateVariables(List<Magma.VariableDto> variables, @Nullable @QueryParam("comment") String comment);

  @DELETE
  Response deleteVariables(@QueryParam("variable") List<String> variables);

  @Path("/locales")
  LocalesResource getLocalesResource();
}
