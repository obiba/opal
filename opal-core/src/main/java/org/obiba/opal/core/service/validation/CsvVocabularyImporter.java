package org.obiba.opal.core.service.validation;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by carlos on 7/28/14.
 */
public class CsvVocabularyImporter extends TextVocabularyImporter {

    @Override
    protected Set<String> parseCodes(BufferedReader reader) throws IOException {
        Set<String> result = new HashSet<>();
        String line = reader.readLine(); //ignore header

        if (line == null) {
            throw new IllegalArgumentException("No content in resource");
        }

        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            result.add(parts[0]);
        }
        return result;
    }
}
