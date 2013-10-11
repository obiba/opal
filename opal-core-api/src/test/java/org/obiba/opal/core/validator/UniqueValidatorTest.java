package org.obiba.opal.core.validator;

import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.internal.util.ReflectionHelper;
import org.junit.Test;
import org.obiba.opal.core.domain.OrientDbEntity;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.SqlDatabase;
import org.obiba.opal.core.service.OrientDbService;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

public class UniqueValidatorTest {

  @Test
  public void testFindAnnotatedClass() {
    assertEquals(null, UniqueValidator.findAnnotatedClass(OrientDbEntity.class));
    assertEquals(Database.class, UniqueValidator.findAnnotatedClass(Database.class));
    assertEquals(Database.class, UniqueValidator.findAnnotatedClass(SqlDatabase.class));
  }

  @Test
  public void testUnique() {

    SqlDatabase existing = new SqlDatabase.Builder().name("db").build();
    existing.setId("1");

    OrientDbService mockOrientDbService = createMock(OrientDbService.class);
    expect(mockOrientDbService.uniqueResult("select from Database where name = ?", "db")).andReturn(existing).once();
    replay(mockOrientDbService);

    Validator validator = getValidator(mockOrientDbService);

    SqlDatabase sqlDatabase = new SqlDatabase.Builder().name("db").usage(Database.Usage.STORAGE).url("url")
        .driverClass("driver").username("user").password("pass").sqlSchema(SqlDatabase.SqlSchema.HIBERNATE).build();

    Set<ConstraintViolation<SqlDatabase>> constraintViolations = validator.validate(sqlDatabase);

    assertEquals(1, constraintViolations.size());
    ConstraintViolation<SqlDatabase> constraintViolation = constraintViolations.iterator().next();
    assertEquals("must be unique", constraintViolation.getMessage());
    assertEquals("{org.obiba.opal.core.validator.Unique.message}", constraintViolation.getMessageTemplate());
    assertEquals("name", constraintViolation.getPropertyPath().toString());

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

}
