/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.fs;

import java.io.File;

import org.apache.commons.vfs.FileNotFoundException;
import org.apache.commons.vfs.FileObject;

/**
 * Opal offers a "file system" in which users may manipulate files without having a user defined in the OS running Opal.
 * That is, all interactions with the underlying file-system go through a unique system-user: the one that runs the Opal
 * server.
 */
public interface OpalFileSystem {

  /**
   * Get the root of the Virtual File System (VFS).
   * 
   * @return A FileObject representing the root.
   */
  public FileObject getRoot();

  /**
   * Gives access to the content of a file from the Virtual File System (VFS) through a local java.io.File.
   * 
   * @param virtualFile A file from the VFS.
   * @return The java.io.File equivalent.
   */
  public File getLocalFile(FileObject virtualFile);

  /**
   * Converts a remote file of the Virtual File System (VFS) by copying its content to a local temporary file.
   * 
   * @param virtualFile The VFS file to "make local".
   * @return A local temporary java.io.File which has the same content has the source VFS file.
   */
  public File convertVirtualFileToLocal(FileObject virtualFile);

  /**
   * Checks if a specific file of the Virtual File System (VFS) is a local file.
   * 
   * @param virtualFile The file to check.
   * @return True if a local file, false if not.
   * 
   * @throws FileNotFoundException An exception is thrown if the specified file does not exist.
   */
  public boolean isLocalFile(FileObject virtualFile) throws FileNotFoundException;

}