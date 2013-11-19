package org.obiba.opal.web.magma;

import java.util.Locale;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.obiba.magma.ValueTable;
import org.obiba.opal.web.model.Magma;

import edu.umd.cs.findbugs.annotations.Nullable;

public interface VariableViewResource {

  void setName(String name);

  void setLocales(Set<Locale> locales);

  void setValueTable(ValueTable valueTable);

  @GET
  Magma.VariableDto get(@Context UriInfo uriInfo);

  @PUT
  Response createOrUpdateVariable(Magma.VariableDto variable, @Nullable @QueryParam("comment") String comment);

  @DELETE
  Response deleteVariable();
}
