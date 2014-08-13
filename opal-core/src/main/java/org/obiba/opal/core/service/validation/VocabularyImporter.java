package org.obiba.opal.core.service.validation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;

/**
 * Represents an importer of vocabulary.
 */
public interface VocabularyImporter {

    Set<String> getCodes(InputStream in) throws IOException;

}
