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

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class NotNullIfAnotherFieldHasValueValidatorTest {

  @Test
  public void test_empty() {
    Validator validator = getValidator();
    Set<ConstraintViolation<Stub>> constraintViolations = validator.validate(new Stub());
    assertThat(constraintViolations).isEmpty();
  }

  @Test
  public void test_no_dependency() {
    Validator validator = getValidator();
    Set<ConstraintViolation<Stub>> constraintViolations = validator.validate(new Stub("foo", null));
    assertThat(constraintViolations).isEmpty();
  }

  @Test
  public void test_string_dependency() {
    Validator validator = getValidator();
    Set<ConstraintViolation<Stub>> constraintViolations = validator.validate(new Stub("OK", null));
    assertThat(constraintViolations).hasSize(1);
    ConstraintViolation<Stub> constraintViolation = constraintViolations.iterator().next();
    assertThat(constraintViolation.getMessage()).isEqualTo("cannot be null if status == OK");
    assertThat(constraintViolation.getMessageTemplate())
        .isEqualTo("{org.obiba.opal.core.validator.NotNullIfAnotherFieldHasValue.message}");
    assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("statusDependant");
  }

  @Test
  public void test_enum_dependency() {
    Validator validator = getValidator();
    Set<ConstraintViolation<Stub>> constraintViolations = validator.validate(new Stub(Stub.Type.TYPE_2, null));
    assertThat(constraintViolations).hasSize(1);
    ConstraintViolation<Stub> constraintViolation = constraintViolations.iterator().next();
    assertThat(constraintViolation.getMessage()).isEqualTo("cannot be null if type == TYPE_2");
    assertThat(constraintViolation.getMessageTemplate())
        .isEqualTo("{org.obiba.opal.core.validator.NotNullIfAnotherFieldHasValue.message}");
    assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("typeDependant");
  }

  private Validator getValidator() {
    return Validation.byDefaultProvider().configure().buildValidatorFactory().getValidator();
  }

  @NotNullIfAnotherFieldHasValue.List({ //
      @NotNullIfAnotherFieldHasValue(fieldName = "status", fieldValue = "OK", dependFieldName = "statusDependant"), //
      @NotNullIfAnotherFieldHasValue(fieldName = "type", fieldValue = "TYPE_2", dependFieldName = "typeDependant") })
  private static class Stub {

    private enum Type {
      TYPE_1, TYPE_2
    }

    private String status;

    private Type type;

    private String statusDependant;

    private String typeDependant;

    private Stub() { }

    private Stub(String status, String statusDependant) {
      this.status = status;
      this.statusDependant = statusDependant;
    }

    private Stub(Type type, String typeDependant) {
      this.type = type;
      this.typeDependant = typeDependant;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public Type getType() {
      return type;
    }

    public void setType(Type type) {
      this.type = type;
    }

    public String getStatusDependant() {
      return statusDependant;
    }

    public void setStatusDependant(String statusDependant) {
      this.statusDependant = statusDependant;
    }

    public String getTypeDependant() {
      return typeDependant;
    }

    public void setTypeDependant(String typeDependant) {
      this.typeDependant = typeDependant;
    }
  }

}
