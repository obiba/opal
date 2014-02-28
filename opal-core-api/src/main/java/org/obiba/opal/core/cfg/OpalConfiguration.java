/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.cfg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;

import org.obiba.magma.support.MagmaEngineFactory;
import org.obiba.runtime.Version;

import com.google.common.collect.Iterables;

public class OpalConfiguration {

  private Version version;

  private String secretKey;

  /**
   * Encrypted password
   */
  private String databasePassword;

  private String fileSystemRoot;

  private MagmaEngineFactory magmaEngineFactory;

  private final Collection<OpalConfigurationExtension> extensions = new ArrayList<>();

  public Version getVersion() {
    return version;
  }

  public void setVersion(Version version) {
    this.version = version;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setFileSystemRoot(String fileSystemRoot) {
    this.fileSystemRoot = fileSystemRoot;
  }

  public String getFileSystemRoot() {
    return fileSystemRoot;
  }

  public MagmaEngineFactory getMagmaEngineFactory() {
    return magmaEngineFactory;
  }

  public void setMagmaEngineFactory(MagmaEngineFactory magmaEngineFactory) {
    this.magmaEngineFactory = magmaEngineFactory;
  }

  public void addExtension(OpalConfigurationExtension extension) {
    if(!hasExtension(extension.getClass())) {
      extensions.add(extension);
    }
  }

  public <T extends OpalConfigurationExtension> T getExtension(Class<T> type) {
    try {
      return Iterables.getOnlyElement(Iterables.filter(extensions, type));
    } catch(IndexOutOfBoundsException e) {
      throw new NoSuchElementException();
    }
  }

  public <T extends OpalConfigurationExtension> boolean hasExtension(Class<T> type) {
    return Iterables.size(Iterables.filter(extensions, type)) == 1;
  }

  /**
   * @return encrypted password
   */
  public String getDatabasePassword() {
    return databasePassword;
  }

  /**
   * @param databasePassword encrypted password
   */
  public void setDatabasePassword(String databasePassword) {
    this.databasePassword = databasePassword;
  }
}