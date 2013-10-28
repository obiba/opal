package org.obiba.opal.core.validator;

public class UniqueValidatorTest {

//  @Test
//  public void testFindAnnotatedClass() {
//    assertEquals(null, UniqueValidator.findAnnotatedClass(OrientDbEntity.class));
//    assertEquals(Database.class, UniqueValidator.findAnnotatedClass(Database.class, "name"));
//    assertEquals(Database.class, UniqueValidator.findAnnotatedClass(SqlSettings.class, "name"));
//    assertEquals(SqlSettings.class, UniqueValidator.findAnnotatedClass(SqlSettings.class, "url"));
//  }
//
//  @Test
//  public void testUnique() {
//
//    SqlSettings existing = new SqlSettings.Builder().name("db").build();
//    existing.setId("1");
//
//    OrientDbService mockOrientDbService = createMock(OrientDbService.class);
//    expect(mockOrientDbService.uniqueResult("select from SqlDatabase where url = ?", "url")).andReturn(null).once();
//    expect(mockOrientDbService.uniqueResult("select from Database where name = ?", "db")).andReturn(existing).once();
//    replay(mockOrientDbService);
//
//    Validator validator = getValidator(mockOrientDbService);
//
//    SqlSettings sqlDatabaseSettings = new SqlSettings.Builder().name("db").usage(Database.Usage.STORAGE).url("url")
//        .driverClass("driver").username("user").password("pass").sqlSchema(SqlSettings.SqlSchema.HIBERNATE).build();
//
//    Set<ConstraintViolation<SqlSettings>> constraintViolations = validator.validate(sqlDatabaseSettings);
//
//    assertEquals(1, constraintViolations.size());
//    ConstraintViolation<SqlSettings> constraintViolation = constraintViolations.iterator().next();
//    assertEquals("must be unique", constraintViolation.getMessage());
//    assertEquals("{org.obiba.opal.core.validator.Unique.message}", constraintViolation.getMessageTemplate());
//    assertEquals("name", constraintViolation.getPropertyPath().toString());
//  }
//
//  private Validator getValidator(final OrientDbService orientDbService) {
//    ValidatorFactory validatorFactory = Validation.byDefaultProvider() //
//        .configure() //
//        .constraintValidatorFactory(new ConstraintValidatorFactory() {
//          // copied from org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorFactoryImpl
//          @Override
//          public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> tClass) {
//            T validator = ReflectionHelper.newInstance(tClass, "ConstraintValidator");
//            if(validator instanceof UniqueValidator) {
//              ((UniqueValidator) validator).setOrientDbService(orientDbService);
//            }
//            return validator;
//          }
//
//          @Override
//          public void releaseInstance(ConstraintValidator<?, ?> constraintValidator) {
//          }
//        }).buildValidatorFactory();
//    return validatorFactory.getValidator();
//  }

}
