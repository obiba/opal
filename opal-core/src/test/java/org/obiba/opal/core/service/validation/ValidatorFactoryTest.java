package org.obiba.opal.core.service.validation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.magma.Variable;
import org.obiba.opal.core.service.MagmaHelper;

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
        factory.postConstruct(); //initialize
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
    
    @Test
    @Ignore //not automated
    public void testGetVocabularyCodesHttp() throws Exception {
        testCsvVocabularyCodes("http://localhost:9090/codes_v1.txt");
    }

    @Test
    @Ignore //not automated
    public void testGetVocabularyCodesHttps() throws Exception {
        testCsvVocabularyCodes("https://185.9.174.105/mica/sites/default/files/codes_v1.txt");
    }

    private void testCsvVocabularyCodes(String url) throws Exception {
        factory.setKeyStore(getTestKeyStore());
        VocabularyImporter importer = new CsvVocabularyImporter();
        Set<String> codes = factory.getVocabularyCodes(new URL(url), importer);
        Assert.assertEquals("codes set should match", VALID_CODES, codes);
    }

    private KeyStore getTestKeyStore() throws IOException, GeneralSecurityException {
        String keyStorePath = "~/.keystore".replace("~", System.getProperty("user.home"));
        String keyStorePassword = "nopassword";

        File file = new File(keyStorePath);

        if (!file.exists()) {
            throw new IllegalArgumentException("KeyStore file not found: " + file.getPath());
        }

        FileInputStream instream = new FileInputStream(file);
        KeyStore store  = KeyStore.getInstance(KeyStore.getDefaultType());

        try {
            store.load(instream, keyStorePassword.toCharArray());
        } finally {
            instream.close();
        }

        return store;
    }

}
