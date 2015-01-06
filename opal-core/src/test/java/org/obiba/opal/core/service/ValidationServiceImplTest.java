package org.obiba.opal.core.service;

import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.*;
import org.obiba.magma.support.StaticValueTable;
import org.obiba.magma.type.TextType;
import org.obiba.opal.core.service.ValidationService.ValidationResult;
import org.obiba.opal.core.service.ValidationService.ValidationTask;
import org.obiba.opal.core.service.validation.ValidatorFactory;
import org.obiba.opal.core.service.validation.VocabularyConstraint;
import org.obiba.opal.core.support.SystemOutMessageLogger;

import java.util.*;

/**
 * Created by carlos on 8/5/14.
 */
public class ValidationServiceImplTest {

    private static final String VALID_CODE = "AAA";
    private static final String INVALID_CODE = "foo";

    private ValidationServiceImpl validationService;

    @Before
    public void setUp() {
        validationService = new ValidationServiceImpl() {
            @Override
            public boolean isValidationEnabled(ValueTable valueTable) {
                return true; //we want to validate everything
            }
        };

        validationService.validatorFactory = new ValidatorFactory();
        validationService.validatorFactory.postConstruct();
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

    /**
     * Creates a table where each value is a sequence with 2 repeated values
     * @param codeValue
     * @param var
     * @return
     * @throws Exception
     */
    private ValueTable createMultivalueTable(String codeValue, Variable var) throws Exception {
        Datasource ds = MagmaHelper.createDatasource(true);
        Set<String> entities = new HashSet<>();
        entities.add("a");
        entities.add("b");
        StaticValueTable table = MagmaHelper.createValueTable(ds, entities, var);
        MagmaHelper.addRow(table, "a", MagmaHelper.VOCAB_VARIABLE, createSequence(VALID_CODE, VALID_CODE));
        MagmaHelper.addRow(table, "b", MagmaHelper.VOCAB_VARIABLE, createSequence(codeValue, codeValue));

        return table;
    }

    private Value createSequence(String ... strings) {
        List<Value> values = new ArrayList<>();
        for (String s: strings) {
            values.add(TextType.get().valueOf(s));
        }
        return TextType.get().sequenceOf(values);
    }

    @Test
    public void testValidateWithVocabularyFailure() throws Exception {
        ValueTable table = createTable(INVALID_CODE, MagmaHelper.createVocabularyVariable());
        ValidationResult result = validationService.validateNoTransaction(table, new SystemOutMessageLogger());

        Assert.assertTrue("should have failures", result.hasFailures());
        Set<List<String>> pairs = result.getFailurePairs();
        Assert.assertEquals("wrong count", 1, pairs.size());
        List<String> pair = pairs.iterator().next();
        Assert.assertEquals("wrong length", 2, pair.size());
        Assert.assertEquals("wrong variable", MagmaHelper.VOCAB_VARIABLE, pair.get(0));
        Assert.assertEquals("wrong rule", VocabularyConstraint.TYPE, pair.get(1));
        Set<Value> failedValues = result.getFailedValues(pair.get(0), pair.get(1));
        Assert.assertEquals("wrong count", 1, failedValues.size());
        Assert.assertEquals("value mismatch", INVALID_CODE, failedValues.iterator().next().toString());
    }

    @Test
    public void testValidateWithVocabularyNoFailures() throws Exception {
        ValueTable table = createTable(VALID_CODE, MagmaHelper.createVocabularyVariable());

        ValidationResult result = validationService.validateNoTransaction(table, new SystemOutMessageLogger());
        checkVariableRules(result);

        Assert.assertFalse("should have no failures", result.hasFailures());
    }

    @Test
    public void testValidateRepeatableFailure() throws Exception {
        ValueTable table = createMultivalueTable(INVALID_CODE, MagmaHelper.createVocabularyVariable());
        ValidationResult result = validationService.validateNoTransaction(table, new SystemOutMessageLogger());

        Assert.assertTrue("should have failures", result.hasFailures());
        Set<List<String>> pairs = result.getFailurePairs();
        Assert.assertEquals("wrong count", 1, pairs.size());
        List<String> pair = pairs.iterator().next();
        Assert.assertEquals("wrong length", 2, pair.size());
        Assert.assertEquals("wrong variable", MagmaHelper.VOCAB_VARIABLE, pair.get(0));
        Assert.assertEquals("wrong rule", VocabularyConstraint.TYPE, pair.get(1));
        Set<Value> failedValues = result.getFailedValues(pair.get(0), pair.get(1));
        Assert.assertEquals("wrong count", 1, failedValues.size());
        Assert.assertEquals("value mismatch", INVALID_CODE, failedValues.iterator().next().toString());
    }

    @Test
    public void testValidateRepeatableNoFailures() throws Exception {
        ValueTable table = createMultivalueTable(VALID_CODE, MagmaHelper.createVocabularyVariable());

        ValidationResult result = validationService.validateNoTransaction(table, new SystemOutMessageLogger());
        checkVariableRules(result);

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

    private void checkVariableRules(ValidationResult result) {
        Map<String, Set<String>> map =  result.getVariableRules();
        Assert.assertEquals("wrong rule map count", 1, map.size());
        Set<String> rules = map.get(MagmaHelper.VOCAB_VARIABLE);
        Assert.assertNotNull("should have rules", rules);
        Set<String> expectedRules = ImmutableSet.of(VocabularyConstraint.TYPE);
        Assert.assertEquals("rules mismatch", expectedRules, rules);
    }

    private boolean isValid(ValueTable valueTable) {
        ValidationResult vd = validationService.validateNoTransaction(valueTable, new SystemOutMessageLogger());
        return vd == null || !vd.hasFailures();
    }

}