package org.obiba.opal.core.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.*;
import org.obiba.magma.support.StaticValueTable;
import org.obiba.opal.core.service.ValidationService.ValidationResult;
import org.obiba.opal.core.service.ValidationService.ValidationTask;
import org.obiba.opal.core.service.validation.VocabularyValidator;
import org.obiba.opal.core.support.MessageLogger;
import org.obiba.opal.core.support.SystemOutMessageLogger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by carlos on 8/5/14.
 */
public class ValidationServiceImplTest {

	private static final String VALID_CODE = "AAA";
	private static final String INVALID_CODE = "foo";

    private ValidationServiceImpl validationService;

    @Before
    public void setUp() {
    	validationService = new ValidationServiceImpl();
    }

    private ValueTable createTable(String codeValue, Variable var) throws Exception {
    	Datasource ds = MagmaHelper.createDatasource(true);
    	Set<String> entities = new HashSet<>();
    	entities.add("a");
    	entities.add("b");
        StaticValueTable table = MagmaHelper.createValueTable(ds, entities, var);
		MagmaHelper.addRow(table, "a", MagmaHelper.VOCAB_VARIABLE, VALID_CODE);
		MagmaHelper.addRow(table, "b", MagmaHelper.VOCAB_VARIABLE, codeValue);
		
		return table;
    }

    @Test
    public void testValidateWithVocabularyFailure() throws Exception {
        ValueTable table = createTable(INVALID_CODE, MagmaHelper.createVocabularyVariable());
        ValidationTask task = validationService.createValidationTask(table, new SystemOutMessageLogger());
        ValidationResult result = task.validate();

		Assert.assertTrue("should have failures", result.hasFailures());
		Set<List<String>> pairs = result.getFailurePairs();
		Assert.assertEquals("wrong count", 1, pairs.size());
		List<String> pair = pairs.iterator().next();
		Assert.assertEquals("wrong length", 2, pair.size());
		Assert.assertEquals("wrong variable", MagmaHelper.VOCAB_VARIABLE, pair.get(0));
		Assert.assertEquals("wrong rule", VocabularyValidator.TYPE, pair.get(1));
		Set<Value> failedValues = result.getFailedValues(pair.get(0), pair.get(1));
		Assert.assertEquals("wrong count", 1, failedValues.size());
		Assert.assertEquals("value mismatch", INVALID_CODE, failedValues.iterator().next().toString());
    }

    @Test
    public void testValidateWithVocabularyNoFailures() throws Exception {
    	ValueTable table = createTable(VALID_CODE, MagmaHelper.createVocabularyVariable());
        ValidationTask task = validationService.createValidationTask(table, new SystemOutMessageLogger());
        ValidationResult result = task.validate();

		Assert.assertFalse("should have no failures", result.hasFailures());
    }

    @Test
    public void testValidateNoValidationNoTask() throws Exception {
        ValueTable table = createTable(INVALID_CODE, MagmaHelper.createVariable());
        ValidationTask task = validationService.createValidationTask(table, new SystemOutMessageLogger());
        Assert.assertNull("should have no validation task", task);
    }

    @Test
    public void testVocabularyIsValidTrue() throws Exception {
        ValueTable table = createTable(VALID_CODE, MagmaHelper.createVocabularyVariable());
        boolean valid = isValid(table);

        Assert.assertTrue("should be valid", valid);
    }

    @Test
    public void testVocabularyIsValidFalse() throws Exception {
        ValueTable table = createTable(INVALID_CODE, MagmaHelper.createVocabularyVariable());
        boolean valid = isValid(table);

        Assert.assertFalse("should be invalid", valid);
    }

    private boolean isValid(ValueTable valueTable) {
        ValidationTask task = validationService.createValidationTask(valueTable, new SystemOutMessageLogger());

        List<String> variables = task.getVariableNames();

        if (variables.isEmpty()) {
            return true; //no variables under validation
        }

        Iterator<ValueSet> valueSets = valueTable.getValueSets().iterator();
        while (valueSets.hasNext()) {
            ValueSet vset = valueSets.next();

            for (String varName: variables) {
                Variable var = valueTable.getVariable(varName);
                Value value = valueTable.getValue(var, vset);
                if (!task.isValid(var, value)) {
                    return false;
                }
            }
        }
        return true;
    }

}
