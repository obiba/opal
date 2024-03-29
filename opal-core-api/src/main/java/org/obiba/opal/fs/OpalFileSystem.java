/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.fs;

import java.io.File;

import javax.validation.constraints.NotNull;

import org.apache.commons.vfs2.FileObject;

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
  @NotNull
  FileObject getRoot();

  /**
   * Gives access to the content of a file from the Virtual File System (VFS) through a local java.io.File.
   *
   * @param virtualFile A file from the VFS.
   * @return The java.io.File equivalent.
   */
  File getLocalFile(FileObject virtualFile);

  /**
   * Gives access to the content of a file from the Virtual File System (VFS) through a local java.io.File.
   *
   * @param virtualPath Path to a path in the VFS.
   * @return
   */
  File resolveLocalFile(String virtualPath);

  /**
   * Converts the path of a file in the Virtual File System (VFS) to an "obfuscated" path an returns it.
   *
   * @param virtualFile The file for which we want to get an obfuscated path.
   * @return The obfuscated path.
   */
  String getObfuscatedPath(FileObject virtualFile);

  /**
   * Searches the Virtual File System (VFS) for a file with a path that corresponds to the obfuscated path and returns
   * it. Returns null if not path is found for the obfuscated path.
   *
   * @param baseFolder The base folder on which the search will be performed.
   * @param obfuscatedPath The obfuscated path.
   * @return The path corresponding to the obfuscated path in the VFS.
   */
  FileObject resolveFileFromObfuscatedPath(FileObject baseFolder, String obfuscatedPath);

  /**
   * Clean file system resources.
   */
  void close();
}