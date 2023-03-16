/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.beanvalidation.SpringConstraintValidatorFactory;

import javax.validation.*;
import java.util.Set;

@Component
public class DefaultBeanValidator implements InitializingBean {

  @Autowired
  private ApplicationContext applicationContext;

  private Validator validator;

  @Override
  public void afterPropertiesSet() throws Exception {
    init();
  }

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
    if(!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }
}
