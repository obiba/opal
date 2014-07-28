package org.obiba.opal.core.service.validation;

import org.junit.Before;
import org.junit.Test;

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
        //String url = "file://foo.csv";
        URI uri = new URI("file", "/", "aaaaaaa.csv");
        String url = uri.toURL().toExternalForm();
        System.out.println(url);
        //InputStream in = new URL(url).openStream();
        //System.out.println(in);


        validateVocab(factory.getVocabularyValidator(url));

    }

    private void validateVocab(VocabularyValidator validator) {

        System.out.println(validator);


    }

}
