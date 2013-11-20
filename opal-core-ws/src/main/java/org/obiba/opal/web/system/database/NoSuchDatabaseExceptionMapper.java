package org.obiba.opal.web.system.database;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.obiba.opal.core.service.database.NoSuchDatabaseException;
import org.springframework.stereotype.Component;

@Component
@Provider
public class NoSuchDatabaseExceptionMapper implements ExceptionMapper<NoSuchDatabaseException> {

  @Override
  public Response toResponse(NoSuchDatabaseException exception) {
    return Response.status(Response.Status.NOT_FOUND).build();
  }
}
