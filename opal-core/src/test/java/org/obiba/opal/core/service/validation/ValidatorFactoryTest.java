package org.obiba.opal.core.service.validation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.type.TextType;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

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
        //File file = new File("opal-core/src/test/resources/vocabulary.csv");
        File file = new File("src/test/resources/vocabulary.csv");
        URI uri = file.toURI();
        String url = uri.toURL().toExternalForm();

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
