/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
