package org.obiba.opal.web.system.database;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.obiba.opal.core.service.database.MultipleIdentifiersDatabaseException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.springframework.stereotype.Component;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Component
@Provider
public class MultipleIdentifiersDatabaseExceptionMapper
    implements ExceptionMapper<MultipleIdentifiersDatabaseException> {

  @Override
  public Response toResponse(MultipleIdentifiersDatabaseException exception) {
    return Response.status(BAD_REQUEST).type("application/x-protobuf+json")
        .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "MultipleIdentifiersDatabase", exception)).build();
  }

}
