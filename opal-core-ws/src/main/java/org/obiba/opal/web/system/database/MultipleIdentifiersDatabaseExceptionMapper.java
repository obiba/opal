package org.obiba.opal.web.system.database;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.obiba.opal.core.service.database.MultipleIdentifiersDatabaseException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.provider.ErrorDtoExceptionMapper;
import org.springframework.stereotype.Component;

import com.google.protobuf.GeneratedMessage;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Component
@Provider
public class MultipleIdentifiersDatabaseExceptionMapper
    extends ErrorDtoExceptionMapper<MultipleIdentifiersDatabaseException> {

  @Override
  protected Response.Status getStatus() {
    return BAD_REQUEST;
  }

  @Override
  protected GeneratedMessage.ExtendableMessage<?> getErrorDto(MultipleIdentifiersDatabaseException exception) {
    return ClientErrorDtos.getErrorMessage(getStatus(), "MultipleIdentifiersDatabase", exception);
  }

}
