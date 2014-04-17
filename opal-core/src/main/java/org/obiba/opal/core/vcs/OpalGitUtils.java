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
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

public final class OpalGitUtils {
  private OpalGitUtils() {}

  public final static String DATA_FOLDER_NAME = "data";

  public final static String GIT_FOLDER_NAME = "git";

  public final static String GIT_VIEWS_NAME = "views";

  private static final Object VIEW_FILE_NAME = "View.xml";

  private static final Object VARIABLE_FILE_EXTENSION = ".js";

  public final static File GIT_ROOT_PATH = new File(
      System.getenv().get("OPAL_HOME") + File.separatorChar + DATA_FOLDER_NAME + File.separatorChar + GIT_FOLDER_NAME +
          File.separatorChar + GIT_VIEWS_NAME);

  public static File getDatasourceGitFolder(@Nonnull String datasourceName) {
    Preconditions.checkArgument(datasourceName != null);
    Map<String, String> v = System.getenv();
    return new File(GIT_ROOT_PATH, datasourceName + ".git");
  }

  public static String getViewFilePath(String view) {
    return view + File.separatorChar + VIEW_FILE_NAME;
  }

  public static String getVariableFilePath(String view, String variable) {
    return view + File.separatorChar + variable + VARIABLE_FILE_EXTENSION;
  }

}
