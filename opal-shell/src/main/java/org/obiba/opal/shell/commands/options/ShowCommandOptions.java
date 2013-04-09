/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell.commands.options;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;

@CommandLineInterface(application = "show")
public interface ShowCommandOptions extends HelpOption {

  @Option(shortName = "d", description = "List the datasources.")
  boolean getDatasources();

  @Option(shortName = "t", description = "List the tables.")
  boolean getTables();

  @Option(shortName = "u", description = "List the functional units.")
  boolean getUnits();
}
