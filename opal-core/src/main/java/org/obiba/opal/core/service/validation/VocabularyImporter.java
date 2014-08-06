package org.obiba.opal.core.service.validation;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

/**
 * Represents an importer of vocabulary.
 */
public interface VocabularyImporter {

    Set<String> getCodes(URL url) throws IOException;

}
