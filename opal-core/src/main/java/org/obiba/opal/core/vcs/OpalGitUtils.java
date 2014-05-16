/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.vcs;

import java.io.File;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

public final class OpalGitUtils {
  private OpalGitUtils() {}

  public static final String HEAD_COMMIT_ID = "HEAD";

  public static final String VARIABLE_FILE_EXTENSION = ".js";

  public static final String VIEW_FILE_NAME = "View.xml";

  public static final String VIEW_WHERE_FILE_NAME = "__where.js";

  private final static String DATA_FOLDER_NAME = "data";

  private final static String WORK_FOLDER_NAME = "work";

  private final static String GIT_FOLDER_NAME = "git";

  private final static String GIT_VIEWS_NAME = "views";

  private final static File GIT_ROOT_PATH = new File(
      System.getProperty("OPAL_HOME") + File.separatorChar + DATA_FOLDER_NAME + File.separatorChar + GIT_FOLDER_NAME +
          File.separatorChar + GIT_VIEWS_NAME);

  private final static File GIT_WORK_PATH = new File(
      System.getProperty("OPAL_HOME") + File.separatorChar + WORK_FOLDER_NAME + File.separatorChar + GIT_FOLDER_NAME +
          File.separatorChar + GIT_VIEWS_NAME);

  public static File getGitDatasourceViewsRepoFolder(@Nonnull String datasourceName) {
    Preconditions.checkArgument(datasourceName != null);
    return new File(GIT_ROOT_PATH, datasourceName + ".git");
  }

  public static File getGitViewsWorkFolder() {
    if (!GIT_WORK_PATH.exists()) GIT_WORK_PATH.mkdirs();
    return GIT_WORK_PATH;
  }

  public static String getViewFilePath(String view) {
    return view + File.separatorChar + VIEW_FILE_NAME;
  }

  public static String getVariableFilePath(String view, String variable) {
    return view + File.separatorChar + variable + VARIABLE_FILE_EXTENSION;
  }

}
