package org.obiba.opal.web.r;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.obiba.opal.r.service.NoSuchRSessionException;
import org.springframework.stereotype.Component;

@Component
@Provider
public class NoSuchRSessionExceptionMapper implements ExceptionMapper<NoSuchRSessionException> {

  @Override
  public Response toResponse(NoSuchRSessionException exception) {
    return Response.status(Status.NOT_FOUND).build();
  }

}
