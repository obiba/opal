/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell.commands;

import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.fs.OpalFileSystem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
public abstract class AbstractOpalRuntimeDependentCommand<T> extends AbstractCommand<T> {
  //
  // AbstractContextLoadingCommand Methods
  //

  @Autowired
  private OpalRuntime opalRuntime;

  //
  // Methods
  //

  /**
   * Initializes the {@link OpalRuntime} bean in the context.
   */
  protected OpalRuntime getOpalRuntime() {
    return opalRuntime;
  }

  protected OpalConfiguration getOpalConfiguration() {
    return opalRuntime.getOpalConfiguration();
  }
  
  public OpalFileSystem getFileSystem() {
    return opalRuntime.getFileSystem();
  }
}
