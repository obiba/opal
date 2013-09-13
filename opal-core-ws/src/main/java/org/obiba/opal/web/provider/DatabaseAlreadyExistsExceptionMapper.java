package org.obiba.opal.web.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.obiba.opal.core.runtime.database.DatabaseAlreadyExistsException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.springframework.stereotype.Component;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Component
@Provider
public class DatabaseAlreadyExistsExceptionMapper implements ExceptionMapper<DatabaseAlreadyExistsException> {

  @Override
  public Response toResponse(DatabaseAlreadyExistsException exception) {
    return Response.status(BAD_REQUEST)
        .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "DatabaseAlreadyExists", exception).build()).build();
  }

}
