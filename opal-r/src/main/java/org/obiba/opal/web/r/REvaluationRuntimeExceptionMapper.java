package org.obiba.opal.web.r;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.obiba.opal.r.REvaluationRuntimeException;
import org.springframework.stereotype.Component;

@Component
@Provider
public class REvaluationRuntimeExceptionMapper implements ExceptionMapper<REvaluationRuntimeException> {

  @Override
  public Response toResponse(REvaluationRuntimeException exception) {
    String message = exception.getMessage() + ": " + exception.getRMessages();
    message = message.replace("\n", "");
    message = message.replace("\r", "");
    return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(message).build();
  }

}
