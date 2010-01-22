/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.cli.client.command;

import org.obiba.opal.core.runtime.OpalRuntime;

/**
 *
 */
public abstract class AbstractOpalRuntimeDependentCommand<T> extends AbstractContextLoadingCommand<T> {
  //
  // Methods
  //

  /**
   * Initializes the {@link OpalRuntime} bean in the context.
   * 
   * Subclasses should call this method after the context has been loaded (i.e., after <code>execute(),
   * typically during somewhere within <code>executeWithContext</code>).
   */
  public void initRuntime() {
    try {
      OpalRuntime opalRuntime = getBean("opalRuntime");
      opalRuntime.init();
    } catch(Exception ex) {
      throw new RuntimeException("Initialization error: " + ex.getMessage());
    }
  }
}
