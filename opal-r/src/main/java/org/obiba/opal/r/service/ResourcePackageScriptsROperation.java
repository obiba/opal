package org.obiba.opal.r.service;

import org.obiba.opal.spi.r.AbstractROperationWithResult;

/**
 * Fetch resource R package names and their location folder.
 */
public class ResourcePackageScriptsROperation extends AbstractROperationWithResult {

  private static final String RESOURCE_JS_FILE = "resources/resource.js";

  @Override
  protected void doWithConnection() {
    setResult(null);
    eval(String.format("is.null(assign('x', lapply(installed.packages()[,1], function(p) { system.file('%s', package=p) })))", RESOURCE_JS_FILE), false);
    setResult(eval("lapply(x[lapply(x, nchar)>0], function(p) { readChar(p, file.info(p)$size) })", false));
  }
}
