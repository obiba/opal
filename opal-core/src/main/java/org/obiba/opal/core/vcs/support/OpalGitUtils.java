package org.obiba.opal.core.vcs.support;

import java.io.File;

public final class OpalGitUtils {
  public final static String OPAL_HOME_SYSTEM_PROPERTY_NAME = "OPAL_HOME";

  public static final String VIEWS_DIRECTORY_NAME = "views";

  public static final String GIT_DIRECTORY_NAME = "git";

  public static final String GIT_EXTENSION= ".git";

  public static File buildOpalGitRootPath() {
    StringBuilder pathBuilder = new StringBuilder();
    pathBuilder.append(System.getProperty(OPAL_HOME_SYSTEM_PROPERTY_NAME)).append(File.separatorChar)
        .append(GIT_DIRECTORY_NAME).append(File.separatorChar).append(VIEWS_DIRECTORY_NAME);

    return new File(pathBuilder.toString());
  }

  public static File getGitDirectoryName(File root, String datasource) {
    return new File(root, datasource + GIT_EXTENSION);
  }
}
