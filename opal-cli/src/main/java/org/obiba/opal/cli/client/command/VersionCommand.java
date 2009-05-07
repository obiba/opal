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

/**
 * Version command.
 */
public class VersionCommand extends AbstractCommand<VersionCommandOptions> {
  //
  // AbstractCommand Methods
  //

  public void execute() {
    // TODO: Get the version from opal-cli's POM.
    System.out.println("version 0.1");
  }

}
