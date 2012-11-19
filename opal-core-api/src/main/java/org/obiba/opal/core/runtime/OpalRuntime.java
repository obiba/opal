/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime;

import java.util.Set;

import org.obiba.opal.fs.OpalFileSystem;

public interface OpalRuntime {

  public Set<Service> getServices();

  public OpalFileSystem getFileSystem();

  public void start();

  public void stop();

  /**
   * True if service with given name is available in Opal Runtime.
   */
  public boolean hasService(String name);

  /**
   * Get the service with the given name.
   *
   * @param name Service name
   * @throw NoSuchService runtime exception if not found (hasService() must be evaluated first)
   */
  public Service getService(String name) throws NoSuchServiceRuntimeException;

}
