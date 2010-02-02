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

import org.obiba.opal.cli.client.command.options.VersionCommandOptions;
import org.obiba.runtime.Version;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Version command.
 */
public class VersionCommand extends AbstractCommand<VersionCommandOptions> {
  //
  // Constants
  //

  @Autowired
  private Version opalVersion;

  //
  // AbstractContextLoadingCommand Methods
  //

  public void execute() {
    System.out.println("The current version of Opal is " + opalVersion);
  }
}
