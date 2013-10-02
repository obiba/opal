package org.obiba.opal.web.gwt.app.client.validation;

import org.obiba.opal.web.model.client.ws.ConstraintViolationErrorDto;

import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.dispatch.annotation.Order;

@GenEvent
public class ConstraintViolationErrors {

  @Order(1)
  Iterable<ConstraintViolationErrorDto> violations;

}
