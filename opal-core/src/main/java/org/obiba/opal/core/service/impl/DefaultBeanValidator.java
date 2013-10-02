package org.obiba.opal.core.service.impl;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;

public class DefaultBeanValidator {

  private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

  private DefaultBeanValidator() {}

  //TODO replace this by Spring/Hibernate-validator method validation with @Valid once Spring 4 is released
  public static <T> void validate(T object, Class<?>... groups) throws ConstraintViolationException {
    Set<ConstraintViolation<T>> violations = VALIDATOR.validate(object, groups);
    if(violations.size() > 0) {
      throw new ConstraintViolationException(violations);
    }
  }

}
