package org.obiba.opal.core.validator;

import java.util.Arrays;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.obiba.opal.core.domain.OrientDbEntity;
import org.obiba.opal.core.service.OrientDbService;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;

public class UniqueValidator implements ConstraintValidator<Unique, OrientDbEntity> {

  @Autowired
  private OrientDbService orientDbService;

  private String[] properties;

  @Override
  public void initialize(Unique unique) {
    properties = unique.properties();
  }

  @Override
  public boolean isValid(OrientDbEntity value, ConstraintValidatorContext context) {
    if(value == null) return true;

    Class<? extends OrientDbEntity> annotatedClass = findAnnotatedClass(value.getClass(), properties);
    PropertyAccessor beanWrapper = new BeanWrapperImpl(value);
    for(String property : properties) {
      String query = String.format("select from %s where %s = ?", annotatedClass.getSimpleName(), property);
      Object propertyValue = beanWrapper.getPropertyValue(property);
      OrientDbEntity existing = orientDbService.uniqueResult(query, propertyValue);
      if(existing != null && !Objects.equal(existing.getId(), value.getId())) {
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
  static Class<? extends OrientDbEntity> findAnnotatedClass(Class<? extends OrientDbEntity> clazz,
      String... properties) {
    if(clazz == null || !OrientDbEntity.class.isAssignableFrom(clazz)) return null;
    if(clazz.isAnnotationPresent(Unique.class)) {
      Unique annotation = clazz.getAnnotation(Unique.class);
      if(Arrays.equals(annotation.properties(), properties)) {
        return clazz;
      }
    }
    return findAnnotatedClass((Class<? extends OrientDbEntity>) clazz.getSuperclass(), properties);
  }

  @VisibleForTesting
  void setOrientDbService(OrientDbService orientDbService) {
    this.orientDbService = orientDbService;
  }
}
