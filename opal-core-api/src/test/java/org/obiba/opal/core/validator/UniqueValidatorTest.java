/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.validator;

import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.easymock.EasyMock;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.junit.Test;
import org.obiba.opal.core.service.OrientDbService;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;

public class UniqueValidatorTest {

  @Test
  public void test_unique_compound() {

    UniqueCompoundStub existing = createUniqueCompoundStub("existing");

    OrientDbService mockOrientDbService = EasyMock.createMock(OrientDbService.class);
    expect(mockOrientDbService
        .uniqueResult(UniqueCompoundStub.class, "select from UniqueCompoundStub where sub1.prop1 = ? or sub2.prop2 = ?",
            "should be unique", "should be unique")).andReturn(existing).once();
    replay(mockOrientDbService);

    Validator validator = getValidator(mockOrientDbService);

    UniqueCompoundStub stub = createUniqueCompoundStub("new stub");

    Set<ConstraintViolation<UniqueCompoundStub>> constraintViolations = validator.validate(stub);
    verify(mockOrientDbService);

    assertThat(constraintViolations).hasSize(1);
    ConstraintViolation<UniqueCompoundStub> constraintViolation = constraintViolations.iterator().next();
    assertThat(constraintViolation.getMessage()).isEqualTo("must be unique");
    assertThat(constraintViolation.getMessageTemplate()).isEqualTo("{org.obiba.opal.core.validator.Unique.message}");
    assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("unique prop");
  }

  private Validator getValidator(final OrientDbService orientDbService) {
    ValidatorFactory validatorFactory = Validation.byDefaultProvider() //
        .configure() //
        .constraintValidatorFactory(new ConstraintValidatorFactory() {
          // copied from org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorFactoryImpl
          @Override
          public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> tClass) {
            T validator = ReflectionHelper.newInstance(tClass, "ConstraintValidator");
            if(validator instanceof UniqueValidator) {
              ((UniqueValidator) validator).setOrientDbService(orientDbService);
            }
            return validator;
          }

          @Override
          public void releaseInstance(ConstraintValidator<?, ?> constraintValidator) {
          }
        }).buildValidatorFactory();
    return validatorFactory.getValidator();
  }

  private UniqueCompoundStub createUniqueCompoundStub(String name) {
    UniqueCompoundStub.Sub1 sub1 = new UniqueCompoundStub.Sub1();
    sub1.setProp1("should be unique");
    UniqueCompoundStub.Sub2 sub2 = new UniqueCompoundStub.Sub2();
    sub2.setProp2("should be unique");
    UniqueCompoundStub stub = new UniqueCompoundStub();
    stub.setName(name);
    stub.setSub1(sub1);
    stub.setSub2(sub2);
    return stub;
  }
}
