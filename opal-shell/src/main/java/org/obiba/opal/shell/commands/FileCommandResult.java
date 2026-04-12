/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.shell.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * A {@link CommandResult} that wraps a single output file (e.g. a zip archive).
 * The parent directory of the file is also removed on {@link #cleanup()} because
 * commands typically create a dedicated timestamp-named work directory.
 */
public class FileCommandResult implements CommandResult {

  private static final Logger log = LoggerFactory.getLogger(FileCommandResult.class);

  private final File file;

  private final String mimeType;

  /**
   * @param file     the result file (must not be {@code null})
   * @param mimeType MIME type used when streaming the file to the client
   */
  public FileCommandResult(File file, String mimeType) {
    if (file == null) throw new IllegalArgumentException("file cannot be null");
    this.file = file;
    this.mimeType = mimeType;
  }

  /**
   * Returns the result file.
   *
   * @return result file
   */
  public File getFile() {
    return file;
  }

  /**
   * Returns the MIME type of the result file.
   *
   * @return MIME type (e.g. {@code "application/zip"})
   */
  public String getMimeType() {
    return mimeType;
  }

  @Override
  public String getLabel() {
    return file.getName();
  }

  /**
   * Deletes the result file and its parent directory (the command's work directory).
   * Failures are logged but not rethrown.
   */
  @Override
  public void cleanup() {
    try {
      if (file.exists() && !file.delete()) {
        log.warn("Could not delete result file: {}", file.getAbsolutePath());
      }
      File parent = file.getParentFile();
      if (parent != null && parent.exists()) {
        File[] remaining = parent.listFiles();
        if (remaining == null || remaining.length == 0) {
          if (!parent.delete()) {
            log.warn("Could not delete work directory: {}", parent.getAbsolutePath());
          }
        }
      }
    } catch (Exception e) {
      log.error("Error during result cleanup for file: {}", file.getAbsolutePath(), e);
    }
  }
}

