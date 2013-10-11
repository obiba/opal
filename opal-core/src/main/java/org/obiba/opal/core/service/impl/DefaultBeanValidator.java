package org.obiba.opal.core.service.impl;

import java.util.Set;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.beanvalidation.SpringConstraintValidatorFactory;

@Component
public class DefaultBeanValidator {

  @Autowired
  private ApplicationContext applicationContext;

  private Validator validator;

  @PostConstruct
  public void init() {
    ValidatorFactory validatorFactory = Validation.byDefaultProvider() //
        .configure() //
        .constraintValidatorFactory(
            new SpringConstraintValidatorFactory(applicationContext.getAutowireCapableBeanFactory())) //
        .buildValidatorFactory();
    validator = validatorFactory.getValidator();
  }

  //TODO replace this by Spring/Hibernate-validator method validation with @Valid once Spring 4 is released
  public <T> void validate(T object, Class<?>... groups) throws ConstraintViolationException {
    Set<ConstraintViolation<T>> violations = validator.validate(object, groups);
    if(violations.size() > 0) {
      throw new ConstraintViolationException(violations);
    }
  }

}
