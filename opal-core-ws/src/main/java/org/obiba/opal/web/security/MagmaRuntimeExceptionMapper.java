package org.obiba.opal.web.security;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.obiba.opal.web.magma.ClientErrorDtos;
import org.springframework.stereotype.Component;

@Provider
@Component
public class MagmaRuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

  @Override
  public Response toResponse(RuntimeException exception) {
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(ClientErrorDtos.getErrorMessage(Response.Status.BAD_REQUEST, "GeneralKeystoreError", exception)).build();
  }
}