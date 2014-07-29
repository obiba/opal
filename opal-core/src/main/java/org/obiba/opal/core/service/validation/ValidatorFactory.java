package org.obiba.opal.core.service.validation;

import org.obiba.magma.Attribute;
import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchAttributeException;
import org.obiba.magma.Variable;
import org.obiba.opal.core.service.MessageLogger;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Created by carlos on 7/29/14.
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
    public List<DataValidator> getValidators(Variable variable, MessageLogger logger) {
        List<DataValidator> result = new ArrayList<>();
        //TODO add type validators

        Attribute attr = null;
        System.out.println(variable.getAttributes().size());
        try {
            attr = variable.getAttribute("vocabulary_url");
        } catch (NoSuchAttributeException ex) {
            //ignored
        }

        if (attr != null) {
            String url = attr.getValue().toString();
            try {
                result.add(getVocabularyValidator(url));
            } catch (Exception ex) {
                logger.error(ex, "Unexpected error obtaining validators for variable %s, url %s", variable.getName(), url);
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
