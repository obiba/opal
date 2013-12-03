/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.validator;

import java.util.Arrays;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.obiba.opal.core.domain.HasUniqueProperties;
import org.obiba.opal.core.service.OrientDbService;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.annotations.VisibleForTesting;

public class UniqueValidator implements ConstraintValidator<Unique, HasUniqueProperties> {

  @Autowired
  private OrientDbService orientDbService;

  private String[] properties;

  @Override
  public void initialize(Unique unique) {
    properties = unique.properties();
  }

  @Override
  public boolean isValid(HasUniqueProperties value, ConstraintValidatorContext context) {
    if(value == null) return true;

    Class<? extends HasUniqueProperties> annotatedClass = findAnnotatedClass(value.getClass(), properties);
    PropertyAccessor beanWrapper = new BeanWrapperImpl(value);
    for(String property : properties) {
      String query = String.format("select from %s where %s = ?", annotatedClass.getSimpleName(), property);
      Object propertyValue = beanWrapper.getPropertyValue(property);
      HasUniqueProperties existing = orientDbService.uniqueResult(value.getClass(), query, propertyValue);
      if(existing != null && !existing.equals(value)) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("{org.obiba.opal.core.validator.Unique.message}") //
            .addPropertyNode(property) //
            .addConstraintViolation();
        return false;
      }
    }
    return true;
  }

  @VisibleForTesting
  @SuppressWarnings("unchecked")
  static Class<? extends HasUniqueProperties> findAnnotatedClass(Class<? extends HasUniqueProperties> clazz,
      String... properties) {
    if(clazz == null || !HasUniqueProperties.class.isAssignableFrom(clazz)) return null;
    if(clazz.isAnnotationPresent(Unique.class)) {
      Unique annotation = clazz.getAnnotation(Unique.class);
      if(Arrays.equals(annotation.properties(), properties)) {
        return clazz;
      }
    }
    return findAnnotatedClass((Class<? extends HasUniqueProperties>) clazz.getSuperclass(), properties);
  }

  @VisibleForTesting
  void setOrientDbService(OrientDbService orientDbService) {
    this.orientDbService = orientDbService;
  }
}