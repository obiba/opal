package org.obiba.opal.core.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

/**
 * Test uniqueness in a class hierarchy for example as Orient will create a table by class,
 * we can't use its unique index to ensure that there is not duplicate between child classes
 */
@Target({ TYPE, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueValidator.class)
@Documented
public @interface Unique {

  /**
   * The property of the entity we want to validate for uniqueness.
   */
  String[] properties();

  String message() default "{org.obiba.opal.core.validator.Unique.message}";

  Class<?>[] groups() default { };

  Class<? extends Payload>[] payload() default { };

}
