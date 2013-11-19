package org.obiba.opal.web.magma;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;

public interface CompareResource {

  void setComparedDatasource(Datasource comparedDatasource);

  void setComparedTable(ValueTable comparedTable);

  @GET
  @Path("/{with}")
  Response compare(@PathParam("with") String with);
}
