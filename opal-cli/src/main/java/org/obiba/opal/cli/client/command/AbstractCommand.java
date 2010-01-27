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

import org.obiba.opal.cli.client.CliClient;

/**
 * Base class for commands.
 */
public abstract class AbstractCommand<T> implements Command<T> {
  //
  // Instance Variables
  //

  private CliClient client;

  protected T options;

  //
  // Methods
  //

  public void setClient(CliClient client) {
    this.client = client;
  }

  public CliClient getClient() {
    return client;
  }

  public void setOptions(T options) {
    this.options = options;
  }
}
