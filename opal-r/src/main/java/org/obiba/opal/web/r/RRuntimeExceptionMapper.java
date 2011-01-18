package org.obiba.opal.web.r;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
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
    if(exception.getCause() != null && exception.getCause().getMessage() != null) {
      if(message == null) {
        message = exception.getCause().getMessage();
      } else {
        message = message + ": " + exception.getCause().getMessage();
      }
    }
    ResponseBuilder response = Response.status(Status.INTERNAL_SERVER_ERROR);

    if(message != null) {
      response.type(MediaType.TEXT_PLAIN).entity(message);
    }

    return response.build();
  }

}
