/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.spi.r;

import com.google.common.collect.Sets;
import org.obiba.opal.spi.r.datasource.magma.MagmaRRuntimeException;

import java.io.File;
import java.util.Set;

/**
 * Read all files from R work directory into a local file.
 */
public class FolderReadROperation extends AbstractROperation {

  private final String folderName;

  private final File destination;

  public FolderReadROperation(File destination) {
    this(".", destination);
  }

  public FolderReadROperation(String folderName, File destination) {
    this.folderName = folderName;
    this.destination = destination;
    if (!destination.exists() && !destination.mkdirs())
      throw new IllegalArgumentException("Cannot create folder: " + destination.getAbsolutePath());
    else if (!destination.isDirectory())
      throw new IllegalArgumentException("Not a destination folder: " + destination.getAbsolutePath());
  }

  @Override
  public void doWithConnection() {
    try {
      // note: hidden files won't be read, folders are excluded, no recursion
      String[] fileNames = eval(String.format("base::list.files(path='%s')", folderName), false).asStrings();
      Set<String> dirNames = Sets.newHashSet(eval(String.format("list.dirs(path='%s', recursive = FALSE, full.names = FALSE)", folderName), false).asStrings());
      for (String fileName : fileNames) {
        if (!dirNames.contains(fileName))
          readFile(fileName, new File(destination, fileName));
      }
    } catch (Exception e) {
      throw new MagmaRRuntimeException("Unable to retrieve content of the R folder: " + folderName, e);
    }
  }

  @Override
  public String toString() {
    return String.format("%s -> %s", folderName, destination);
  }
}
