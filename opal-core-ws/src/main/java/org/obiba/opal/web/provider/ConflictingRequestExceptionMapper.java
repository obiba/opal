package org.obiba.opal.web.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.obiba.opal.web.support.ConflictingRequestException;
import org.springframework.stereotype.Component;

@Component
@Provider
public class ConflictingRequestExceptionMapper implements ExceptionMapper<ConflictingRequestException> {

  @Override
  public Response toResponse(ConflictingRequestException exception) {
    return Response.status(Status.CONFLICT).build();
  }
}
