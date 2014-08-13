package org.obiba.opal.core.service.validation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Set;

/**
 * Generic VocabularyImporter impl bases on text content, using Readers.
 */
public abstract class TextVocabularyImporter implements VocabularyImporter {

    @Override
    public final Set<String> getCodes(InputStream in) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        try {
            return parseCodes(reader);
        } finally {
            try {
                reader.close();
            } catch (IOException ignored) {
                //late or irrelevant failure
            }
        }
    }

    protected abstract Set<String> parseCodes(BufferedReader reader) throws IOException;

}
