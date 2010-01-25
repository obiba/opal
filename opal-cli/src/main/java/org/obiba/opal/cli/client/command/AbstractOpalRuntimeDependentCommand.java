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
  // AbstractContextLoadingCommand Methods
  //

  /**
   * Overrides the <code>afterContextLoaded</code> method to first initialize the Opal runtime environment.
   */
  @Override
  protected void afterContextLoaded() {
    initRuntime();
    super.afterContextLoaded();
  }

  //
  // Methods
  //

  /**
   * Initializes the {@link OpalRuntime} bean in the context.
   */
  protected void initRuntime() {
    try {
      OpalRuntime opalRuntime = getBean("opalRuntime");
      opalRuntime.init();
    } catch(Exception ex) {
      throw new RuntimeException("Initialization error: " + ex.getMessage());
    }
  }
}
