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

  private final static String GIT_TAXONOMIES_NAME = "taxonomies";

  public static final String TAXONOMY_FILE_NAME = "taxonomy.yml";

  private final static File GIT_ROOT_PATH = new File(System.getProperty("OPAL_HOME"),
      DATA_FOLDER_NAME + File.separatorChar + GIT_FOLDER_NAME);

  private final static File GIT_WORK_PATH = new File(System.getProperty("OPAL_HOME"),
      WORK_FOLDER_NAME + File.separatorChar + GIT_FOLDER_NAME);

  private final static File GIT_VIEWS_ROOT_PATH = new File(GIT_ROOT_PATH, GIT_VIEWS_NAME);

  private final static File GIT_VIEWS_WORK_PATH = new File(GIT_WORK_PATH, GIT_VIEWS_NAME);

  private final static File GIT_TAXONOMIES_ROOT_PATH = new File(GIT_ROOT_PATH, GIT_TAXONOMIES_NAME);

  private final static File GIT_TAXONOMIES_WORK_PATH = new File(GIT_WORK_PATH, GIT_TAXONOMIES_NAME);

  //
  // Views
  //

  public static File getGitViewsRepoFolder(@Nonnull String datasourceName) {
    return getGitRepoFolder(GIT_VIEWS_ROOT_PATH, datasourceName);
  }

  public static File getGitViewsWorkFolder() {
    return ensureGitWorkFolder(GIT_VIEWS_WORK_PATH);
  }

  public static String getVariableFilePath(String view, String variable) {
    return view + File.separatorChar + variable + VARIABLE_FILE_EXTENSION;
  }

  //
  // Taxonomies
  //

  public static File getGitTaxonomiesRepoFolder() {
    return GIT_TAXONOMIES_ROOT_PATH;
  }

  public static File getGitTaxonomyRepoFolder(@Nonnull String taxonomyName) {
    return getGitRepoFolder(GIT_TAXONOMIES_ROOT_PATH, taxonomyName);
  }

  public static File getGitTaxonomiesWorkFolder() {
    return ensureGitWorkFolder(GIT_TAXONOMIES_WORK_PATH);
  }

  //
  // Private methods
  //

  private static File getGitRepoFolder(File rootPath, @Nonnull String name) {
    Preconditions.checkArgument(name != null);
    return new File(rootPath, name + ".git");
  }

  private static File ensureGitWorkFolder(File workPath) {
    if(!workPath.exists()) workPath.mkdirs();
    return workPath;
  }

}
