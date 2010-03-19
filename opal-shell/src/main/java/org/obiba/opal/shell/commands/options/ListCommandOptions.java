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

import java.util.List;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

@CommandLineInterface(application = "list")
public interface ListCommandOptions extends HelpOption {

  @Unparsed(name = "TABLES")
  public List<String> getTables();

  @Option(shortName = "o", description = "The file to which the metadata will be written.  If not specified, a file will be created with the default name : 'variables-yyyyMMdd_HHmmss.xls'")
  public String getOutputFile();

  public boolean isOutputFile();

}
