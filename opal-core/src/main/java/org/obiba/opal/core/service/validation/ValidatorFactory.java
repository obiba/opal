package org.obiba.opal.core.service.validation;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obiba.magma.Attribute;
import org.obiba.magma.NoSuchAttributeException;
import org.obiba.magma.Variable;
import org.obiba.opal.core.service.ValidationService;

/**
 * Knows how to create DataValidators for a Variable.
 * Datasource and variable attributes will determine if validation is enabled, and which validators the variable has.
 */
public class ValidatorFactory {

    private static final Map<String, VocabularyImporter> importerMap = new HashMap<>();

    static {
        VocabularyImporter csvImporter = new CsvVocabularyImporter();
        importerMap.put("csv", csvImporter);
        importerMap.put("txt", csvImporter);
    }

    /**
     * Returns the list of validators for a given Variable.
     *
     * @param variable
     * @return list of validators
     */
    public List<DataValidator> getValidators(Variable variable) {
        List<DataValidator> result = new ArrayList<>();
        //TODO add type validators

        Attribute attr = null;
        try {
            attr = variable.getAttribute(ValidationService.VOCABULARY_URL_ATTRIBUTE);
        } catch (NoSuchAttributeException ex) {
            //ignored
        }

        if (attr != null) {
            String url = attr.getValue().toString();
            try {
                result.add(getVocabularyValidator(url));
            } catch (Exception ex) {
                String msg = 
                		String.format("Unexpected error obtaining validators for variable %s, url %s", variable.getName(), url);
                throw new RuntimeException(msg, ex);
            }
        }

        return result;
    }

    /**
     * Returns a vocabulary validator for a given url.
     *
     * @param url
     * @return
     * @throws IOException
     */
    public VocabularyValidator getVocabularyValidator(String url) throws IOException {
        int idx = url.lastIndexOf('.');
        if (idx < 0) {
            throw new IllegalArgumentException("Could not obtain filename extension from " + url);
        }
        String extension = url.substring(idx + 1).toLowerCase();
        VocabularyImporter importer = importerMap.get(extension);

        if (importer == null) {
            throw new UnsupportedOperationException("File extension " + extension + " is not supported");
        }

        URL url2 = new URL(url);
        String name = url2.getPath();
        Set<String> codes = importer.getCodes(url2);
        return new VocabularyValidator(name, codes);
    }

}
