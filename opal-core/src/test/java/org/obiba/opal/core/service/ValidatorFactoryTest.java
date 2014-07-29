package org.obiba.opal.core.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.type.TextType;
import org.obiba.opal.core.service.validation.ValidatorFactory;
import org.obiba.opal.core.service.validation.VocabularyValidator;

import java.io.File;
import java.net.URI;

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
    public void testFileVocab() throws Exception {
        File file = new File("src/test/resources/vocabulary.csv");
        URI uri = file.toURI();
        String url = uri.toURL().toExternalForm();

        validateVocab(factory.getVocabularyValidator(url));
    }

    @Test
    public void testHttpVocab() throws Exception {
        String url = "http://localhost:9090/codes_v1.txt";
        validateVocab(factory.getVocabularyValidator(url));
    }

    private void validateVocab(VocabularyValidator validator) {

        Assert.assertTrue(validator.isValid(TextType.get().valueOf("AAA")));
        Assert.assertTrue(validator.isValid(TextType.get().valueOf("BBB")));
        Assert.assertTrue(validator.isValid(TextType.get().valueOf("CCC")));
        Assert.assertFalse(validator.isValid(TextType.get().valueOf("aaa")));
        Assert.assertFalse(validator.isValid(TextType.get().valueOf("foo")));
        Assert.assertFalse(validator.isValid(TextType.get().valueOf("bar")));
    }

}
