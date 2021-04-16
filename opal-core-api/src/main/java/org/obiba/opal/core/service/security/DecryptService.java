/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service.security;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.obiba.opal.core.service.NoSuchIdentifiersMappingException;

/**
 * Service for decrypt operations.
 */
public interface DecryptService {
  /**
   * Decrypts data into an Opal datasource.
   *
   * @param projectName name of the project (this determines this keystore should be used)
   * @param datasourceName name of the destination datasource
   * @param file data file to be decrypted
   * @throws org.obiba.opal.core.service.NoSuchIdentifiersMappingException if the specified unit does not exist
   * @throws IllegalArgumentException if the specified datasource does not exist
   * @throws IOException if the specified file does not exist or is not a normal file
   */
  void decryptData(String projectName, String datasourceName, FileObject file)
      throws NoSuchIdentifiersMappingException, IllegalArgumentException, IOException;

  /**
   * Decrypt data using the system keystore.
   * @param datasourceName
   * @param file
   * @throws NoSuchIdentifiersMappingException
   * @throws IllegalArgumentException
   * @throws IOException
   */
  void decryptData(String datasourceName, FileObject file)
      throws NoSuchIdentifiersMappingException, IllegalArgumentException, IOException;

}
