/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime.jdbc;

import com.google.common.base.Strings;
import jakarta.annotation.Nullable;
import org.obiba.opal.core.service.database.InvalidH2DatabaseException;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * H2 databases are embedded: they live in files owned by the Opal server, in a single folder. Users register them by
 * name only ({@code jdbc:h2:file:<name>}), and that short form is what gets persisted; the name is expanded to an
 * absolute path in the H2 folder when the connection is opened. Restricting the URL to a plain name is what keeps the
 * databases inside that folder: a path separator or a parent reference cannot be expressed, so there is nothing to
 * escape with.
 * <p>
 * The name is the whole URL: H2 settings, which a {@code ;} would introduce, are not accepted. They are not a way of
 * tuning the connection but a second language, in which {@code INIT} alone runs arbitrary SQL — {@code RUNSCRIPT FROM}
 * a remote URL included — every time the connection is opened. The same setting can be passed as a connection
 * property, so {@link #validateProperties(String)} rejects it there too, along with the two settings that would take
 * away the fsync H2 performs when the database is closed.
 * <p>
 * A database owned by a project takes a second form, {@code jdbc:h2:file:<project name>/data}: a folder of its own
 * under the H2 folder, holding one database. The folder is what makes the two kinds coexist - a folder {@code Foo/}
 * and a file {@code Foo.mv.db} are not the same name - and what makes deletion a folder removal rather than a guess at
 * which files beside {@code Foo.mv.db} belong to it. The folder name is a project name, which
 * {@code ProjectsResource.createProject} has already constrained to letters, digits, underscore, space and hyphen:
 * no separator, no parent reference, no {@code ;}.
 */
public final class H2DatabaseUrls {

  public static final String DRIVER_CLASS = "org.h2.Driver";

  private static final String FILE_PREFIX = "jdbc:h2:file:";

  /**
   * A single file name, not starting with a dot, with no path separator, no drive or protocol separator, and no H2
   * settings separator.
   */
  private static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z0-9_-][A-Za-z0-9._-]*");

  /**
   * A project name, as {@code ProjectsResource.createProject} enforces it. Repeated here rather than referenced
   * because this class is what turns the name into a path: the two must be read together, and a loosening of the
   * REST-side rule has to fail here before it reaches the file system.
   */
  private static final Pattern PROJECT_NAME_PATTERN = Pattern.compile("[\\w _-]+");

  /**
   * The name of the one database inside a project's folder. Fixed, so that the folder name carries the project name
   * and nothing else has to be parsed.
   */
  private static final String PROJECT_FILE = "data";

  /**
   * H2 setting that runs SQL statements when a connection is opened.
   */
  private static final String INIT_SETTING = "INIT";

  /**
   * H2 setting that decides whether the JVM shutdown hook closes the database. Left at its default, it is one of the
   * two ways the store is written to physical disk; turned off, the hook falls back to a checkpoint that flushes
   * without forcing, and a power loss right after a clean stop can still lose the last writes.
   */
  private static final String DB_CLOSE_ON_EXIT_SETTING = "DB_CLOSE_ON_EXIT";

  /**
   * H2 setting that decides how long the database stays open after the last connection is closed. Left at 0, closing
   * the connection pool is enough to close the database; anything else keeps it open past the point where Opal
   * believes it has released it.
   */
  private static final String DB_CLOSE_DELAY_SETTING = "DB_CLOSE_DELAY";

  /**
   * H2 1.x page store file suffix, unreadable by the H2 2.x driver that Opal ships.
   */
  private static final String LEGACY_SUFFIX = ".h2.db";

  private static final String SUFFIX = ".mv.db";

  private H2DatabaseUrls() {
  }

  public static boolean isH2(@Nullable String driverClass) {
    return DRIVER_CLASS.equals(driverClass);
  }

  /**
   * Extract the database name from a {@code jdbc:h2:file:<name>} URL, rejecting anything that is not a plain name.
   *
   * @throws InvalidH2DatabaseException if the URL is not of the expected form
   */
  public static String getDatabaseName(@Nullable String url) {
    if(Strings.isNullOrEmpty(url) || !url.startsWith(FILE_PREFIX)) {
      throw new InvalidH2DatabaseException("H2 database URL must be of the form " + FILE_PREFIX + "<name>");
    }
    String name = url.substring(FILE_PREFIX.length());
    if(!NAME_PATTERN.matcher(name).matches()) {
      throw new InvalidH2DatabaseException(
          "H2 database name must be a plain file name, made of letters, digits, '.', '_' or '-', with no H2 setting " +
              "appended to it: '" + name + "'");
    }
    return name;
  }

  /**
   * Verify that the URL names a database that the H2 driver shipped by Opal can open.
   *
   * @throws InvalidH2DatabaseException if the URL is not of the expected form, or names an H2 1.x database
   */
  public static void validate(@Nullable String url, File h2Root) {
    String name = getDatabaseName(url);
    if(new File(h2Root, name + LEGACY_SUFFIX).exists() && !new File(h2Root, name + SUFFIX).exists()) {
      throw new InvalidH2DatabaseException(
          "H2 database '" + name + "' is in the H2 1.x format and must be migrated to H2 2.x");
    }
  }

  /**
   * Verify that the connection properties, a {@code ;} separated list of {@code name=value} pairs handed to the driver
   * as they are, carry none of the settings Opal reserves for itself: H2 reads its settings from the properties as
   * well as from the URL, so the properties are the remaining way of expressing them.
   * <p>
   * {@code INIT} is refused because it runs arbitrary SQL. {@code DB_CLOSE_ON_EXIT} and {@code DB_CLOSE_DELAY} are
   * refused because H2 only writes the store to physical disk when the database is closed — {@code FileStore.stop()}
   * ends in a {@code FileChannel.force(true)} — and these are the two settings that stop that close from happening.
   * Turning either off would silently take away the durability everything else assumes.
   *
   * @throws InvalidH2DatabaseException if a reserved setting is present
   */
  public static void validateProperties(@Nullable String properties) {
    if(Strings.isNullOrEmpty(properties)) return;
    for(String property : properties.split(";")) {
      int idx = property.indexOf('=');
      String name = (idx < 0 ? property : property.substring(0, idx)).trim();
      String value = idx < 0 ? "" : property.substring(idx + 1).trim();
      if(INIT_SETTING.equalsIgnoreCase(name)) {
        throw new InvalidH2DatabaseException(
            "The H2 INIT setting is not allowed: it runs SQL statements every time the connection is opened");
      }
      if(DB_CLOSE_ON_EXIT_SETTING.equalsIgnoreCase(name) && !"TRUE".equalsIgnoreCase(value)) {
        throw new InvalidH2DatabaseException("The H2 " + DB_CLOSE_ON_EXIT_SETTING +
            " setting cannot be turned off: the database would no longer be written to disk when the JVM stops");
      }
      if(DB_CLOSE_DELAY_SETTING.equalsIgnoreCase(name) && !"0".equals(value)) {
        throw new InvalidH2DatabaseException("The H2 " + DB_CLOSE_DELAY_SETTING +
            " setting cannot be changed: the database would stay open, and unwritten, after Opal has released it");
      }
    }
  }

  /**
   * Turn {@code jdbc:h2:file:<name>} into an absolute URL in the H2 folder. The folder is created if missing, as H2
   * does not create it.
   */
  public static String expand(@Nullable String url, File h2Root) {
    String name = getDatabaseName(url);
    if(!h2Root.exists() && !h2Root.mkdirs() && !h2Root.exists()) {
      throw new InvalidH2DatabaseException("Cannot create the H2 databases folder: " + h2Root.getAbsolutePath());
    }
    return FILE_PREFIX + new File(h2Root, name).getAbsolutePath();
  }

  /**
   * The URL of the database owned by a project: a folder named after the project, holding one database.
   *
   * @throws InvalidH2DatabaseException if the project name cannot be a folder name
   */
  public static String projectUrl(String projectName) {
    return FILE_PREFIX + validProjectName(projectName) + "/" + PROJECT_FILE;
  }

  /**
   * Extract the project name from a {@code jdbc:h2:file:<project name>/data} URL.
   *
   * @throws InvalidH2DatabaseException if the URL is not of the expected form
   */
  public static String getProjectName(@Nullable String url) {
    if(Strings.isNullOrEmpty(url) || !url.startsWith(FILE_PREFIX)) {
      throw new InvalidH2DatabaseException(
          "The URL of a project database must be of the form " + FILE_PREFIX + "<project name>/" + PROJECT_FILE);
    }
    String path = url.substring(FILE_PREFIX.length());
    int idx = path.lastIndexOf('/');
    if(idx < 0 || !PROJECT_FILE.equals(path.substring(idx + 1))) {
      throw new InvalidH2DatabaseException(
          "The URL of a project database must end with '/" + PROJECT_FILE + "': '" + path + "'");
    }
    return validProjectName(path.substring(0, idx));
  }

  /**
   * The folder holding a project's database, whether or not it exists.
   *
   * @throws InvalidH2DatabaseException if the project name cannot be a folder name, or if the folder would not sit
   * inside the H2 folder
   */
  public static File projectFolder(String projectName, File h2Root) {
    File folder = new File(h2Root, validProjectName(projectName));
    assertInside(folder, h2Root);
    return folder;
  }

  /**
   * Verify that the URL names a project database that the H2 driver shipped by Opal can open. Reachable only from a
   * hand-edited configuration: Opal writes this URL itself.
   *
   * @throws InvalidH2DatabaseException if the URL is not of the expected form, or names an H2 1.x database
   */
  public static void validateProject(@Nullable String url, File h2Root) {
    File folder = projectFolder(getProjectName(url), h2Root);
    if(new File(folder, PROJECT_FILE + LEGACY_SUFFIX).exists() &&
        !new File(folder, PROJECT_FILE + SUFFIX).exists()) {
      throw new InvalidH2DatabaseException(
          "H2 database '" + folder.getName() + "' is in the H2 1.x format and must be migrated to H2 2.x");
    }
  }

  /**
   * Turn {@code jdbc:h2:file:<project name>/data} into an absolute URL in the H2 folder. The project folder is created
   * if missing, as H2 does not create it.
   */
  public static String expandProject(@Nullable String url, File h2Root) {
    File folder = projectFolder(getProjectName(url), h2Root);
    if(!folder.exists() && !folder.mkdirs() && !folder.exists()) {
      throw new InvalidH2DatabaseException("Cannot create the project database folder: " + folder.getAbsolutePath());
    }
    return FILE_PREFIX + new File(folder, PROJECT_FILE).getAbsolutePath();
  }

  private static String validProjectName(@Nullable String projectName) {
    if(projectName == null || !PROJECT_NAME_PATTERN.matcher(projectName).matches()) {
      throw new InvalidH2DatabaseException(
          "A project database folder is named after its project, made of letters, digits, '_', ' ' or '-': '" +
              projectName + "'");
    }
    return projectName;
  }

  /**
   * The pattern makes this unreachable in practice. It is here so that a future loosening of the project name rule
   * cannot turn into a path traversal without a test failing first.
   */
  private static void assertInside(File folder, File h2Root) {
    try {
      String root = h2Root.getCanonicalPath();
      String path = folder.getCanonicalPath();
      if(!path.startsWith(root.endsWith(File.separator) ? root : root + File.separator)) {
        throw new InvalidH2DatabaseException(
            "A project database folder must sit inside " + root + ": " + path);
      }
    } catch(IOException e) {
      throw new InvalidH2DatabaseException("Cannot resolve the project database folder: " + e.getMessage());
    }
  }
}
