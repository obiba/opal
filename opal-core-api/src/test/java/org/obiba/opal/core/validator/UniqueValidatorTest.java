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
import org.obiba.opal.core.domain.HasUniqueProperties;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.SqlSettings;
import org.obiba.opal.core.service.OrientDbService;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

public class UniqueValidatorTest {

  @Test
  public void testFindAnnotatedClass() {
    assertEquals(null, UniqueValidator.findAnnotatedClass(HasUniqueProperties.class));
    assertEquals(Database.class, UniqueValidator.findAnnotatedClass(Database.class, "settings.url"));
  }

  @Test
  public void testUnique() {

    Database existing = createSqlDatabase("existing database", "url");
    OrientDbService mockOrientDbService = EasyMock.createMock(OrientDbService.class);
    expect(mockOrientDbService.uniqueResult(Database.class, "select from Database where settings.url = ?", "url"))
        .andReturn(existing).once();
    replay(mockOrientDbService);

    Validator validator = getValidator(mockOrientDbService);

    Database database = createSqlDatabase("new database", "url");

    Set<ConstraintViolation<Database>> constraintViolations = validator.validate(database);
    verify(mockOrientDbService);

    assertEquals(1, constraintViolations.size());
    ConstraintViolation<Database> constraintViolation = constraintViolations.iterator().next();
    assertEquals("must be unique", constraintViolation.getMessage());
    assertEquals("{org.obiba.opal.core.validator.Unique.message}", constraintViolation.getMessageTemplate());
    assertEquals("settings.url", constraintViolation.getPropertyPath().toString());
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

  private Database createSqlDatabase(String name, String url) {
    return Database.Builder.create() //
        .name(name) //
        .usedForIdentifiers(false) //
        .defaultStorage(true) //
        .usage(Database.Usage.IMPORT) //
        .sqlSettings(SqlSettings.Builder.create() //
            .sqlSchema(SqlSettings.SqlSchema.HIBERNATE) //
            .driverClass("mysql") //
            .url(url) //
            .username("root") //
            .password("password") //
            .properties("props")) //
        .build();
  }

}
