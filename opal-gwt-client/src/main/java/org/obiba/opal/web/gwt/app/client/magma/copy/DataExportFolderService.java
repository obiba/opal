package org.obiba.opal.web.gwt.app.client.magma.copy;


import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;

@Singleton
public class DataExportFolderService {

  private final RequestCredentials credentials;

  private final String defaultExportFolder;
  private String projectExportFolder;

  @Inject
  public DataExportFolderService(RequestCredentials credentials) {
    this.credentials = credentials;
    defaultExportFolder = "/home/" + credentials.getUsername() + "/export";
  }

  public void setProjectExportFolder(String projectExportFolder) {
    this.projectExportFolder = projectExportFolder;
  }

  public String getExportFolder() {
    return Strings.isNullOrEmpty(projectExportFolder) ? defaultExportFolder : projectExportFolder;
  }
}
