package org.obiba.opal.web.gwt.app.client.validation;

import java.util.Collection;
import java.util.List;

import org.obiba.opal.web.model.client.ws.ClientErrorDto;
import org.obiba.opal.web.model.client.ws.ConstraintViolationErrorDto;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArray;

public class ConstraintViolationUtils {

  private ConstraintViolationUtils() {}

  public static Collection<ConstraintViolationErrorDto> parseErrors(ClientErrorDto error) {

    List<ConstraintViolationErrorDto> violations = Lists.newArrayList();

    @SuppressWarnings("unchecked")
    JsArray<ConstraintViolationErrorDto> errors = (JsArray<ConstraintViolationErrorDto>) error
        .getExtension(ConstraintViolationErrorDto.ClientErrorDtoExtensions.errors);
    if(errors != null) {
      for(int i = 0; i < errors.length(); i++) {
        violations.add(errors.get(i));
      }
    }
    return violations;
  }

}
