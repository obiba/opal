package org.obiba.opal.core.service.validation;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private static final Set<String> VALID_CODES = new HashSet<>();
    static {
        VALID_CODES.add("AAA");
        VALID_CODES.add("BBB");
        VALID_CODES.add("CCC");
    }

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

        for (String code: VALID_CODES) {
            Assert.assertTrue(validator.isValid(MagmaHelper.valueOf(code)));
        }
        Assert.assertFalse(validator.isValid(MagmaHelper.valueOf("aaa")));
        Assert.assertFalse(validator.isValid(MagmaHelper.valueOf("foo")));
        Assert.assertFalse(validator.isValid(MagmaHelper.valueOf("bar")));
    }
    
    //@Test
    public void testGetVocabularyCodesHttp() throws Exception {
        testCsvVocabularyCodes("http://localhost:9090/codes_v1.txt");
    }

    //@Test
    public void testGetVocabularyCodesHttps() throws Exception {
        testCsvVocabularyCodes("https://185.9.174.105/mica/sites/default/files/codes_v1.txt");
    }

    private void testCsvVocabularyCodes(String url) throws Exception {
        VocabularyImporter importer = new CsvVocabularyImporter();
        Set<String> codes = factory.getVocabularyCodes(url, importer);
        Assert.assertEquals("codes set should match", VALID_CODES, codes);
    }

}
