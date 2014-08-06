package org.obiba.opal.core.service.validation;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Variable;
import org.obiba.opal.core.service.MagmaHelper;

/**
 * Created by carlos on 7/28/14.
 */
public class ValidatorFactoryTest {

    private ValidatorFactory factory;

    @Before
    public void setUp() {
        factory = new ValidatorFactory();
    }

    @Test
    public void testGetValidators() throws Exception  {
    	Variable variable = MagmaHelper.createVocabularyVariable(); 
		List<DataValidator> validators = factory.getValidators(variable);
		Assert.assertEquals("wrong count", 1, validators.size());
    	checkVocabValidator(validators.get(0));
    }

    @Test
    public void testGetVocabValidator() throws Exception  {
    	VocabularyValidator validator = factory.getVocabularyValidator(MagmaHelper.getVocabularyFileUrl());
    	checkVocabValidator(validator);
    }
    
    private void checkVocabValidator(DataValidator validator) {

        Assert.assertTrue(validator.isValid(MagmaHelper.valueOf("AAA")));
        Assert.assertTrue(validator.isValid(MagmaHelper.valueOf("BBB")));
        Assert.assertTrue(validator.isValid(MagmaHelper.valueOf("CCC")));
        Assert.assertFalse(validator.isValid(MagmaHelper.valueOf("aaa")));
        Assert.assertFalse(validator.isValid(MagmaHelper.valueOf("foo")));
        Assert.assertFalse(validator.isValid(MagmaHelper.valueOf("bar")));
    }
    
    /*
    /*
    @Test
    public void testHttpVocab() throws Exception {
        String url = "http://localhost:9090/codes_v1.txt";
        validateVocab(factory.getVocabularyValidator(url));
    }
    */

}
