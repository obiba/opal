package org.obiba.opal.core.vcs.support;

import java.io.File;

public final class OpalGitUtils {

  public static final Object VIEW_FILE_NAME = "View.xml";

  public static final Object VARIABLE_FILE_EXTENSION = ".js";

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

  public static String getViewFilePath(String view) {
    return new StringBuilder().append(view).append(File.separatorChar).append(VIEW_FILE_NAME).toString();
  }

  public static String getVariableFilePath(String view, String variable) {
    return new StringBuilder().append(view).append(File.separatorChar).append(variable)
        .append(VARIABLE_FILE_EXTENSION).toString();
  }

  public static String getNthCommitId(String commitId, int nth) {
    return commitId +"~" + String.valueOf(nth);
  }


}
