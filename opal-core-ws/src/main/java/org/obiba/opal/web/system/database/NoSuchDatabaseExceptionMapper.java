package org.obiba.opal.web.system.database;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.obiba.opal.core.service.database.NoSuchDatabaseException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.model.Ws;
import org.springframework.stereotype.Component;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Component
@Provider
public class NoSuchDatabaseExceptionMapper implements ExceptionMapper<NoSuchDatabaseException> {

  @Override
  public Response toResponse(NoSuchDatabaseException exception) {
    Ws.ClientErrorDto errorDto = ClientErrorDtos.getErrorMessage(NOT_FOUND, "NoSuchDatabase")
        .addArguments(exception.getDatabaseName()).build();
    return Response.status(NOT_FOUND).entity(errorDto).type("application/x-protobuf+json").build();
  }
}
