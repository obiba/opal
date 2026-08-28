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
import java.util.regex.Pattern;

/**
 * H2 databases are embedded: they live in files owned by the Opal server, in a single folder. Users register them by
 * name only ({@code jdbc:h2:file:<name>}), and that short form is what gets persisted; the name is expanded to an
 * absolute path in the H2 folder when the connection is opened. Restricting the URL to a plain name is what keeps the
 * databases inside that folder: a path separator or a parent reference cannot be expressed, so there is nothing to
 * escape with.
 */
public final class H2DatabaseUrls {

  public static final String DRIVER_CLASS = "org.h2.Driver";

  private static final String FILE_PREFIX = "jdbc:h2:file:";

  /**
   * A single file name, not starting with a dot, with no path separator and no drive or protocol separator.
   */
  private static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z0-9_-][A-Za-z0-9._-]*");

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
    String name = stripSettings(url.substring(FILE_PREFIX.length()));
    if(!NAME_PATTERN.matcher(name).matches()) {
      throw new InvalidH2DatabaseException(
          "H2 database name must be a plain file name, made of letters, digits, '.', '_' or '-': '" + name + "'");
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
   * Turn {@code jdbc:h2:file:<name>} into an absolute URL in the H2 folder, keeping any H2 settings appended to it.
   * The folder is created if missing, as H2 does not create it.
   */
  public static String expand(@Nullable String url, File h2Root) {
    String name = getDatabaseName(url);
    if(!h2Root.exists() && !h2Root.mkdirs() && !h2Root.exists()) {
      throw new InvalidH2DatabaseException("Cannot create the H2 databases folder: " + h2Root.getAbsolutePath());
    }
    String settings = url.substring(FILE_PREFIX.length() + name.length());
    return FILE_PREFIX + new File(h2Root, name).getAbsolutePath() + settings;
  }

  private static String stripSettings(String spec) {
    int idx = spec.indexOf(';');
    return idx < 0 ? spec : spec.substring(0, idx);
  }
}
