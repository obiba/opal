package org.obiba.opal.web.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.vfs.FileSystemException;
import org.springframework.stereotype.Component;

@Component
@Provider
public class FileSystemExceptionMapper implements ExceptionMapper<FileSystemException> {

  @Override
  public Response toResponse(FileSystemException exception) {
    return Response.status(Status.NOT_FOUND).entity(exception.getMessage()).build();
  }

}
