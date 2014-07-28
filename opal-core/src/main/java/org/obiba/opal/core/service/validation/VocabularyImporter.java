package org.obiba.opal.core.service.validation;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

/**
 * Created by carlos on 7/28/14.
 */
public interface VocabularyImporter {

    Set<String> getCodes(URL url) throws IOException;

}
