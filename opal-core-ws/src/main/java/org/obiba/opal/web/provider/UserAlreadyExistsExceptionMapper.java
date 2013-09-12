package org.obiba.opal.web.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.obiba.opal.core.service.impl.UserAlreadyExistsException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.springframework.stereotype.Component;

import static javax.ws.rs.core.Response.Status.CONFLICT;

@Component
@Provider
public class UserAlreadyExistsExceptionMapper implements ExceptionMapper<UserAlreadyExistsException> {

  @Override
  public Response toResponse(UserAlreadyExistsException exception) {
    return Response.status(CONFLICT).entity(ClientErrorDtos.getErrorMessage(CONFLICT, exception.getMessage()).build())
        .build();
  }

}
