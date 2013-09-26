package org.obiba.opal.web.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.obiba.opal.core.runtime.database.IdentifiersDatabaseNotFoundException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.springframework.stereotype.Component;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Component
@Provider
public class IdentifiersDatabaseNotFoundExceptionMapper
    implements ExceptionMapper<IdentifiersDatabaseNotFoundException> {

  @Override
  public Response toResponse(IdentifiersDatabaseNotFoundException exception) {
    return Response.status(BAD_REQUEST)
        .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "IdentifiersDatabaseNotFound", exception).build()).build();
  }

}
