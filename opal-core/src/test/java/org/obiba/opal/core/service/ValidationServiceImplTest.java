package org.obiba.opal.core.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.StaticValueTable;
import org.obiba.opal.core.service.ValidationService.ValidationResult;
import org.obiba.opal.core.service.validation.VocabularyValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by carlos on 8/5/14.
 */
//@ContextConfiguration(classes = ValidationServiceImplTest.Config.class)
public class ValidationServiceImplTest /*extends AbstractJUnit4SpringContextTests*/ {

	private static final String VALID_CODE = "AAA";
	private static final String INVALID_CODE = "foo";

	//@Autowired
    private ValidationServiceImpl validationService;

    @Before
    public void setUp() {
    	validationService = new ValidationServiceImpl();
    }

    private ValueTable createTableForVocabularyTest(String codeValue) throws Exception {
    	Datasource ds = MagmaHelper.createDatasource(true);
    	Set<String> entities = new HashSet<>();
    	entities.add("a");
    	entities.add("b");
    	
    	StaticValueTable table = MagmaHelper.createValueTable(ds, entities, MagmaHelper.createVocabularyVariable());
		MagmaHelper.addRow(table, "a", MagmaHelper.VOCAB_VARIABLE, VALID_CODE);
		MagmaHelper.addRow(table, "b", MagmaHelper.VOCAB_VARIABLE, codeValue);
		
		return table;
    }
    
    @Test
    public void testValidateWithVocabularyFailure() throws Exception {
    	ValidationResult collector = new ValidationResult();
    	ValueTable table = createTableForVocabularyTest(INVALID_CODE);

		validationService.validate(table, collector);
		Assert.assertTrue("should have failures", collector.hasFailures());
		List<List<String>> pairs = collector.getFailurePairs();
		Assert.assertEquals("wrong count", 1, pairs.size());
		List<String> pair = pairs.get(0);
		Assert.assertEquals("wrong length", 2, pair.size());
		Assert.assertEquals("wrong variable", MagmaHelper.VOCAB_VARIABLE, pair.get(0));
		Assert.assertEquals("wrong rule", VocabularyValidator.NAME, pair.get(1));
		Set<Value> failedValues = collector.getFailedValues(pair);
		Assert.assertEquals("wrong count", 1, failedValues.size());
		Assert.assertEquals("value mismatch", INVALID_CODE, failedValues.iterator().next().toString());
    }
    
    @Test
    public void testValidateNoFailures() throws Exception {
    	ValidationResult collector = new ValidationResult();
    	ValueTable table = createTableForVocabularyTest(VALID_CODE);

		validationService.validate(table, collector);
		Assert.assertFalse("should have no failures", collector.hasFailures());
    }
    
    @Configuration
    public static class Config extends AbstractOrientDbTestConfig {
        @Bean
        public ValidationService taxonomyService() {
            return new ValidationServiceImpl();
        }
    }
}
