package org.obiba.opal.core.service.validation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Set;

/**
 * Created by carlos on 7/28/14.
 */
public abstract class TextVocabularyImporter implements VocabularyImporter {

    public final Set<String> getCodes(URL url) throws IOException {

        InputStream in = url.openStream();
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
