/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.validator;

import java.util.Arrays;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.obiba.opal.core.domain.HasUniqueProperties;
import org.obiba.opal.core.service.OrientDbService;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.annotations.VisibleForTesting;

public class UniqueValidator implements ConstraintValidator<Unique, HasUniqueProperties> {

  @Autowired
  private OrientDbService orientDbService;

  private Unique unique;

  @Override
  public void initialize(@SuppressWarnings("ParameterHidesMemberVariable") Unique unique) {
    this.unique = unique;
  }

  @Override
  @SuppressWarnings("RedundantIfStatement")
  public boolean isValid(HasUniqueProperties value, ConstraintValidatorContext context) {
    if(value == null) {
      return true;
    }
    if(unique.properties() != null && !isValidProperties(value, context)) {
      return false;
    }
    if(unique.compoundProperties() != null && !isValidCompoundProperties(value, context)) {
      return false;
    }
    return true;
  }

  private boolean isValidProperties(HasUniqueProperties value, ConstraintValidatorContext context) {
    Class<? extends HasUniqueProperties> annotatedClass = findAnnotatedClass(value.getClass());
    PropertyAccessor beanWrapper = new BeanWrapperImpl(value);
    for(String property : unique.properties()) {
      String query = String.format("select from %s where %s = ?", annotatedClass.getSimpleName(), property);
      Object propertyValue = beanWrapper.getPropertyValue(property);
      HasUniqueProperties existing = orientDbService.uniqueResult(value.getClass(), query, propertyValue);
      if(existing != null && !existing.equals(value)) {
        buildConstraintViolation(context, property);
        return false;
      }
    }
    return true;
  }

  private boolean isValidCompoundProperties(HasUniqueProperties value, ConstraintValidatorContext context) {
    Class<? extends HasUniqueProperties> annotatedClass = findAnnotatedClass(value.getClass());
    PropertyAccessor beanWrapper = new BeanWrapperImpl(value);
    for(Unique.CompoundProperty compoundProperty : unique.compoundProperties()) {
      HasUniqueProperties existing = findExisting(value, annotatedClass, beanWrapper, compoundProperty);
      if(existing != null && !existing.equals(value)) {
        buildConstraintViolation(context, compoundProperty.name());
        return false;
      }
    }
    return true;
  }

  private HasUniqueProperties findExisting(HasUniqueProperties value,
      Class<? extends HasUniqueProperties> annotatedClass, PropertyAccessor beanWrapper,
      Unique.CompoundProperty compoundProperty) {
    StringBuilder query = new StringBuilder("select from " + annotatedClass.getSimpleName() + " where ");
    Object propertyValue = null;
    int length = compoundProperty.properties().length;
    for(int i = 0; i < length; i++) {
      String property = compoundProperty.properties()[i];
      query.append(property).append(" = ?");
      if(beanWrapper.isReadableProperty(property)) {
        propertyValue = beanWrapper.getPropertyValue(property);
      }
      if(i + 1 < length) query.append(" or ");
    }
    Object[] args = new Object[length];
    for(int i = 0; i < length; i++) {
      args[i] = propertyValue;
    }
    return orientDbService.uniqueResult(value.getClass(), query.toString(), args);
  }

  private void buildConstraintViolation(ConstraintValidatorContext context, String property) {
    context.disableDefaultConstraintViolation();
    context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
        .addPropertyNode(property) //
        .addConstraintViolation();
  }

  @SuppressWarnings("unchecked")
  private Class<? extends HasUniqueProperties> findAnnotatedClass(Class<? extends HasUniqueProperties> clazz) {
    if(clazz == null || !HasUniqueProperties.class.isAssignableFrom(clazz)) return null;
    if(clazz.isAnnotationPresent(Unique.class)) {
      Unique annotation = clazz.getAnnotation(Unique.class);
      if(Arrays.equals(annotation.properties(), unique.properties()) &&
          Arrays.equals(annotation.compoundProperties(), unique.compoundProperties())) {
        return clazz;
      }
    }
    return findAnnotatedClass((Class<? extends HasUniqueProperties>) clazz.getSuperclass());
  }

  @VisibleForTesting
  void setOrientDbService(OrientDbService orientDbService) {
    this.orientDbService = orientDbService;
  }
}