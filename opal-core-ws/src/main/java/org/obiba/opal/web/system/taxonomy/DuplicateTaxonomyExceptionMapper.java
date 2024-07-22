package org.obiba.opal.web.system.taxonomy;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.obiba.opal.core.cfg.DuplicateTaxonomyException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.model.Ws;
import org.obiba.opal.web.provider.ErrorDtoExceptionMapper;
import org.springframework.stereotype.Component;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

@Component
@Provider
public class DuplicateTaxonomyExceptionMapper extends ErrorDtoExceptionMapper<DuplicateTaxonomyException> {

  @Override
  protected Response.Status getStatus() {
    return BAD_REQUEST;
  }

  @Override
  protected Ws.ClientErrorDto getErrorDto(DuplicateTaxonomyException exception) {
    return ClientErrorDtos.getErrorMessage(getStatus(), "DuplicateTaxonomy", exception);
  }

}
