package org.obiba.opal.web.r;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.obiba.opal.r.RRuntimeException;
import org.springframework.stereotype.Component;

@Component
@Provider
public class RRuntimeExceptionMapper implements ExceptionMapper<RRuntimeException> {

  @Override
  public Response toResponse(RRuntimeException exception) {
    String message = exception.getMessage();
    if(exception.getCause() != null) {
      message = message + ": " + exception.getCause().getMessage();
    }
    return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(message).build();
  }

}
