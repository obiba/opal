package org.obiba.opal.web.system.taxonomy;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.obiba.opal.core.service.TaxonomyAlreadyExistsException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.provider.ErrorDtoExceptionMapper;
import org.springframework.stereotype.Component;

import com.google.protobuf.GeneratedMessage;

import static javax.ws.rs.core.Response.Status.CONFLICT;

@Component
@Provider
public class TaxonomyAlreadyExistsExceptionMapper extends ErrorDtoExceptionMapper<TaxonomyAlreadyExistsException> {

  @Override
  protected Response.Status getStatus() {
    return CONFLICT;
  }

  @Override
  protected GeneratedMessage.ExtendableMessage<?> getErrorDto(TaxonomyAlreadyExistsException exception) {
    return ClientErrorDtos.getErrorMessage(getStatus(), "TaxonomyAlreadyExists").addArguments(exception.getName())
        .build();
  }

}
