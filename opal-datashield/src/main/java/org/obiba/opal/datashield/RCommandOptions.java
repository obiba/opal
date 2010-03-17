/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datashield;

import org.obiba.opal.shell.commands.options.HelpOption;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;

@CommandLineInterface(application = "r")
public interface RCommandOptions extends HelpOption {

  @Option(shortName = "t", longName = "table", description = "Add table [table] to the R engine")
  public String getTable();

  public boolean isTable();

  @Option(shortName = "e", longName = "eval", description = "Evaluate <cmd>")
  public String getEval();

  public boolean isEval();
}
