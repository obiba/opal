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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Test uniqueness in a class hierarchy for example as Orient will create a table by class,
 * we can't use its unique index to ensure that there is not duplicate between child classes
 */
@Target({ TYPE })
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
