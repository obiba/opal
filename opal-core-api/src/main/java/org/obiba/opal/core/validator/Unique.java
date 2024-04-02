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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueValidator.class)
@Documented
public @interface Unique {

  /**
   * The property of the entity we want to validate for uniqueness.
   */
  String[] properties() default { };

  CompoundProperty[] compoundProperties() default { };

  String message() default "{org.obiba.opal.core.validator.Unique.message}";

  Class<?>[] groups() default { };

  Class<? extends Payload>[] payload() default { };

  @Retention(RetentionPolicy.RUNTIME)
  @interface CompoundProperty {

    String name();

    String[] properties();

  }

}
