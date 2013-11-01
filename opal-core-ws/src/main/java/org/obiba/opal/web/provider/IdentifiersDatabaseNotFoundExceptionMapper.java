package org.obiba.opal.web.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.obiba.opal.core.service.database.IdentifiersDatabaseNotFoundException;
import org.springframework.stereotype.Component;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Component
@Provider
public class IdentifiersDatabaseNotFoundExceptionMapper
    implements ExceptionMapper<IdentifiersDatabaseNotFoundException> {

  @Override
  public Response toResponse(IdentifiersDatabaseNotFoundException exception) {
    return Response.status(NOT_FOUND).build();
  }

}
