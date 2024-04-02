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

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessor;

/**
 * Implementation of {@link NotNullIfAnotherFieldHasValue} validator.
 * <p/>
 * See http://stackoverflow.com/questions/9284450/jsr-303-validation-if-one-field-equals-something-then-these-other-fields-sho
 */
public class NotNullIfAnotherFieldHasValueValidator
    implements ConstraintValidator<NotNullIfAnotherFieldHasValue, Object> {

  private String fieldName;

  private String expectedFieldValue;

  private String dependFieldName;

  @Override
  public void initialize(NotNullIfAnotherFieldHasValue annotation) {
    fieldName = annotation.fieldName();
    expectedFieldValue = annotation.fieldValue();
    dependFieldName = annotation.dependFieldName();
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    if(value == null) {
      return true;
    }

    PropertyAccessor beanWrapper = new BeanWrapperImpl(value);
    Object fieldValue = beanWrapper.getPropertyValue(fieldName);
    Object dependFieldValue = beanWrapper.getPropertyValue(dependFieldName);

    if(fieldValue != null && fieldValue.toString().equals(expectedFieldValue) && dependFieldValue == null) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
          .addPropertyNode(dependFieldName).addConstraintViolation();

      return false;
    }

    return true;
  }

}